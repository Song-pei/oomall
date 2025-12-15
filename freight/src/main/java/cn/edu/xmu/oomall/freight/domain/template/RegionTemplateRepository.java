//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.domain.template;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.oomall.freight.domain.TemplateRepository;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import cn.edu.xmu.oomall.freight.domain.RegionRepository;
import cn.edu.xmu.oomall.freight.domain.bo.template.RegionTemplate;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.RegionTemplatePoMapper;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.TemplatePoMapper;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.TemplateRegionPoMapper;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.TemplateRegionPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.Constants.MAX_RETURN;
import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

/**
 * 运费模板的dao
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RegionTemplateRepository {
    private static final String KEY = "R%d";
    private static final String REGIONKEY = "T%dR%d";

    @Value("${oomall.freight.region-template.timeout}")
    private Long timeout;

    private final RegionTemplatePoMapper regionTemplatePoMapper;
    private final TemplateRegionPoMapper templateRegionPoMapper;
    private final TemplatePoMapper templatePoMapper;
    private final TemplateRepository templateRepository;
    private final ApplicationContext context;
    private final RegionRepository regionRepository;
    private final TemplateRegionRepository templateRegionRepository;
    private final RedisUtil redisUtil;



    /**
     * 方便测试将build方法的类型改为public
     * 设置RegionTemplate的分包策略和打包属性
     *
     * @param template
     * @param po
     * @param redisKey
     * @return
     * @author ZhaoDong Wang
     * 2023-dgn1-009
     */
    public RegionTemplate build(Template template, RegionTemplatePo po, Optional<String> redisKey) {
        TemplateRepositoryInf repository = this.findTemplateBean(template);
        RegionTemplate bo = repository.getRegionTemplate(po);
        this.build(bo);
        log.debug("getBo: bo = {}", bo);
        redisKey.ifPresent(key -> redisUtil.set(key, bo, timeout));
        return bo;
    }

    public RegionTemplate build(RegionTemplate bo) {
        bo.setRegionRepository(this.regionRepository);
        bo.setTemplateRepository(this.templateRepository);
        bo.setTemplateRegionRepository(this.templateRegionRepository);
        return bo;
    }

    /**
     * 根据关键字找到运费模板
     *
     * @param id
     * @return
     * @throws RuntimeException
     * <p>
     * date: 2022-11-22 12:22
     */
    public Optional<RegionTemplate> findById(Long id) throws RuntimeException {
        String key = String.format(KEY, id);
        RegionTemplate bo = (RegionTemplate) redisUtil.get(key);
        if(Objects.nonNull(bo)){
            this.build(bo);
            return Optional.of(bo);
        }else {
            log.debug("findById: id = {}", id);
            return this.regionTemplatePoMapper.findById(id)
                    .map(po->{
                        Template template = this.templateRepository.findById(PLATFORM, po.getTemplateId())
                                .orElseThrow(() -> new BusinessException(ReturnNo.INCONSISTENT_DATA));
                        return build(template, po, Optional.ofNullable(key));
                    });
        }
    }

    /**
     * 根据运费模板id和地区id来查找地区模板信息
     * 如果没有与rid对应的地区模板，不会继续查询上级地区模板
     *
     * @param template 运费模板
     * @param rid      地区id
     * @throws RuntimeException
     */
    public Optional<RegionTemplate> retrieveByTemplateAndRegionId(Template template, Long rid) throws RuntimeException {
        String key = String.format(REGIONKEY, template.getId(), rid);
        //先用rid和tid的redisKey来寻找对应的地区模板id
        Long regionTemplateId = (Long) this.redisUtil.get(key);
        RegionTemplate regionTemplate;
        if (Objects.isNull(regionTemplateId)) {
            regionTemplate = this.regionTemplatePoMapper.findByTemplateIdAndRegionId(template.getId(), rid)
                    .map(regionTemplatePo -> {
                        String regionTemplateKey = String.format(KEY, regionTemplatePo.getId());
                        return this.build(template, regionTemplatePo, Optional.of(regionTemplateKey));
                    }).orElse(null);
            this.redisUtil.set(key, regionTemplate.getId(), timeout);
        } else {
            String regionTemplateKey = String.format(KEY, regionTemplateId);
            RegionTemplate redisObj = (RegionTemplate) this.redisUtil.get(regionTemplateKey);
            regionTemplate = this.build(redisObj);
        }

        log.debug("retrieveByTemplateIdAndRegionId: regionTemplate={}", regionTemplate);
        return Optional.ofNullable(regionTemplate);
    }

    /**
     * 根据模板id查找所有的地区模板信息
     *
     * @param template 模板
     * @param page
     * @param pageSize
     * @throws RuntimeException
     */
    public List<RegionTemplate> retrieveByTemplate(Template template, Integer page, Integer pageSize) {
        List<RegionTemplatePo> ret = null;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        log.debug("retrieveByTemplateId:page={},pageSize={}", pageable.getPageNumber(), pageable.getPageSize());
        ret = this.regionTemplatePoMapper.findByTemplateId(template.getId(), pageable);

        log.debug("retrieveRegionTemplateById: po = {}", ret);
        List<RegionTemplate> templateList = ret.stream()
                .map(po -> this.build(template, po, Optional.ofNullable(null)))
                .collect(Collectors.toList());
        return templateList;
    }

    public List<RegionTemplate> retrieveByTemplate(Template template) {
        List<RegionTemplatePo> ret = null;
        ret = this.regionTemplatePoMapper.findByTemplateId(template.getId());
        log.debug("retrieveRegionTemplateById: po = {}", ret);
        List<RegionTemplate> templateList = ret.stream()
                .map(po -> this.build(template, po, Optional.ofNullable(null)))
                .collect(Collectors.toList());
        return templateList;
    }

    /**
     * 修改模板
     *
     * @param bo
     * @throws RuntimeException
     * @author Ming Qiu
     * <p>
     * date: 2022-11-22 17:14
     */
    public String update(Template template, RegionTemplate bo){
        log.debug("update: bo ={}", bo);
        String key = String.format(KEY, bo.getId());
        bo.setModifier(null);
        bo.setGmtModified(Instant.now());
        TemplateRepositoryInf repository = this.findTemplateBean(template);
        RegionTemplatePo oldPo = this.regionTemplatePoMapper.findById(bo.getId()).orElseThrow(()->new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "运费模板", bo.getId())));
        RegionTemplatePo po = CloneFactory.copyNotNull(oldPo, bo);
        this.regionTemplatePoMapper.save(po);
        repository.update(bo);

        if (Objects.nonNull(bo.getRegionIds())) {
            this.templateRegionPoMapper.deleteAllByRegionTemplateId(template.getId());
            this.insertRegionIds(bo);
        }
        return key;
    }

    /**
     * 删除地区模板
     *
     * @param template
     * @param rid
     * @throws RuntimeException
     */
    public List<String> delete(Template template, Long rid) {
        log.debug("delRegionByTemplateIdAndRegionId: template ={},rid={}", template, rid);
        List<String> delKeys = new ArrayList<>();
        Optional<RegionTemplatePo> ret = regionTemplatePoMapper.findByTemplateIdAndRegionId(template.getId(), rid);
        log.debug("delRegionByTemplateIdAndRegionId: ret ={}", ret);
        if (ret.isPresent()) {
            RegionTemplatePo po = ret.get();
            String key = String.format(KEY, rid,template.getId());
            TemplateRepositoryInf dao = this.findTemplateBean(template);
            regionTemplatePoMapper.deleteById(po.getId());
            if (redisUtil.hasKey(key))
                redisUtil.del(key);
            delKeys.add(key);
            dao.delete(po.getObjectId());
        } else {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        return delKeys;
    }

    /**
     * 删除运费模板，同步删除该模板所拥有的所有地区模板
     *
     * @param template
     * @throws RuntimeException
     */
    public List<String> deleteTemplate(Template template) throws RuntimeException {
        log.debug("deleteTemplate: template ={}", template);
        List<String> delKeys = new ArrayList<>();
        this.templatePoMapper.deleteById(template.getId());
        TemplateRepositoryInf repository = this.findTemplateBean(template);

        Integer page = 0, pageSize = MAX_RETURN;
        while (pageSize.equals(MAX_RETURN)) {
            Pageable pageable = PageRequest.of(page, pageSize);
            List<RegionTemplatePo> ret = regionTemplatePoMapper.findByTemplateId(template.getId(), pageable);
            for (RegionTemplatePo po : ret) {
                String key = String.format(KEY, template.getId());
                delKeys.add(key);
                repository.delete(po.getObjectId());
            }
            page = page + 1;
            pageSize = ret.size();
        }
        this.regionTemplatePoMapper.deleteAllByTemplateId(template.getId());

        return delKeys;
    }

    /**
     * 新增模板
     * @param template
     * @param bo
     * <p>
     */
    public RegionTemplate insert(Template template, RegionTemplate bo){
        log.debug("insert: bo ={}", bo);
        bo.setCreator(null);
        bo.setGmtCreate(Instant.now());
        RegionTemplatePo po = CloneFactory.copyNotNull(new RegionTemplatePo(), bo);

        TemplateRepositoryInf repository = this.findTemplateBean(template);
        String objectId = repository.insert(bo);
        log.debug("insert: objectId = {}",objectId);
        po.setObjectId(objectId);
        bo.setObjectId(objectId);
        log.debug("insert: po = {}", po);
        RegionTemplatePo newPo = this.regionTemplatePoMapper.save(po);
        log.debug("insert: newPo = {}", newPo);
        bo.setId(newPo.getId());

        insertRegionIds(bo);
        return bo;
    }

    /**
     * 插入regionIds
     * @param bo
     */
    private void insertRegionIds(RegionTemplate bo) {
        try{
            for (Long regionId : bo.getRegionIds()){
                TemplateRegionPo regionPo = new TemplateRegionPo();
                regionPo.setRegionId(regionId);
                regionPo.setRegionTemplateId(bo.getId());
                this.templateRegionPoMapper.save(regionPo);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ReturnNo.FREIGHT_REGIONEXIST, String.format(ReturnNo.FREIGHT_REGIONEXIST.getMessage()));
        }
    }

    /**
     * 返回Bean对象
     *
     * @param template
     * @return
     * @author Ming Qiu
     * <p>
     * date: 2022-11-22 16:11
     */
    private TemplateRepositoryInf findTemplateBean(Template template) {
        return (TemplateRepositoryInf) context.getBean(template.getTemplateBean());
    }
}
