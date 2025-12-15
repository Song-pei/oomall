//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.application;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.freight.application.vo.FreightPriceVo;
import cn.edu.xmu.oomall.freight.application.vo.ProductItemVo;
import cn.edu.xmu.oomall.freight.domain.TemplateRepository;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import cn.edu.xmu.oomall.freight.domain.bo.Region;
import cn.edu.xmu.oomall.freight.domain.RegionRepository;
import cn.edu.xmu.oomall.freight.domain.bo.template.*;
import cn.edu.xmu.oomall.freight.domain.template.RegionTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;


@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class RegionTemplateService {

    private final TemplateRepository templateRepository;
    private final RegionRepository regionRepository;
    private final RedisUtil redisUtil;
    private final RegionTemplateRepository regionTemplateRepository;
    private final TemplateReuseService templateReuseService;

    /**
     * 管理员修改重量或件数模板明细
     * @param shopId
     * @param bo
     */
    public void updateRegionTemplate(Long shopId, RegionTemplate bo, List<Long> regionIds, Class clazz){

        Template template= this.templateReuseService.findTemplateById(shopId, bo.getTemplateId());
        if (!template.gotType().getClass().equals(clazz)){
            throw new BusinessException(ReturnNo.FREIGHT_TEMPLATENOTMATCH);
        }

        List<Long> ids = getValidRegionId(regionIds);
        bo.setRegionIds(ids);
        String key = this.regionTemplateRepository.update(template, bo);
        this.redisUtil.del(key);
    }

    /**
     * 管理员定义重量或件数模板明细
     * @param shopId
     * @param regionTemplate
     */
    public void insertRegionTemplate(Long shopId, RegionTemplate regionTemplate, List<Long> regionIds, Class clazz){
        log.debug("insertRegionTemplate: regionTemplate={}",regionTemplate);
        List<Long> ids = getValidRegionId(regionIds);
        regionTemplate.setRegionIds(ids);
        Template template = this.templateReuseService.findTemplateById(shopId, regionTemplate.getTemplateId());
        this.regionTemplateRepository.insert(template, regionTemplate);
    }

    /**
     * 管理员删除地区模板
     * @param shopId 商铺id
     * @param rid  地区模板id
     */
    public void deleteRegionTemplate(Long shopId,Long rid){
        RegionTemplate regionTemplate = this.regionTemplateRepository.findById(rid)
                .orElseThrow(() -> new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST,
                        String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "地区模板",rid)));
        log.debug("deleteRegionTemplate-regionTemplate:{}",regionTemplate);
        this.regionTemplateRepository.delete(rid);
    }

    /**
     * 店家或管理员查询运费模板下属所有地区模板明细
     * @param shopId 商铺id
     * @param templateId 模板id
     * @param page
     * @param pageSize
     */
    public List<RegionTemplate> retrieveRegionTemplateById(Long shopId, Long templateId, Integer page, Integer pageSize){
        Template template= this.templateReuseService.findTemplateById(shopId, templateId);
        return this.regionTemplateRepository.retrieveByTemplate(template, page, pageSize);
    }

    /**
     * 克隆模板时，其关联的运费模板也需要克隆
     * @param id
     * @param shopId
     * @param user
     */
    public IdNameTypeVo cloneTemplate(Long id, Long shopId, UserToken user) {

        Template template = this.templateReuseService.findTemplateById(shopId,id);

        //克隆template
        template.setId(null);
        template.setDefaultModel((byte) 0);
        Template template1 = this.templateRepository.insert(template,user);

        //按template将所有关联的regionTemplate取出，将其id和objectId设为0重新插入数据库
        this.regionTemplateRepository.retrieveByTemplate(template).stream().forEach(regionTemplate -> {
            regionTemplate.setTemplateId(template1.getId());
            regionTemplate.setObjectId(null);
            this.regionTemplateRepository.insert(template1, regionTemplate);
        });

        return IdNameTypeVo.builder().id(template1.getId()).name(template1.getName()).build();
    }

    /**
     * 计算一批商品的运费
     *
     * @param items
     * @param templateId 模板id
     * @param regionId   地区id
     */
    public FreightPriceVo cacuFreightPrice(List<ProductItem> items, Long templateId, Long regionId) {
        RegionTemplate regionTemplate = this.findRegionTemplate(templateId, regionId);
        log.debug("getFreight: regionTemplate={}", regionTemplate);

        Collection<TemplateResult> ret = regionTemplate.calculate(items);

        long fee = ret.stream().mapToLong(pack -> pack.getFee()).sum();
        List<List<ProductItemVo>> packs = ret.stream().map(pack -> pack.getPack().stream().map(bo -> CloneFactory.copy(new ProductItemVo(), bo)).collect(Collectors.toList())).collect(Collectors.toList());
        packs = packs.stream().map(pack -> pack.stream().sorted(Comparator.comparingLong(ProductItemVo::getOrderItemId)).collect(Collectors.toList())).collect(Collectors.toList());
        return FreightPriceVo.builder().freightPrice(fee).pack(packs).build();
    }

    /**
     * 根据运费模板id和地区id来查找地区模板信息
     * 如果没有与rid对应的地区模板，则会继续查询rid最近的上级地区模板
     * 用于计算运费
     *
     * @param regionId 地区id
     * @return 地区运费模板
     */
    private RegionTemplate findRegionTemplate(Long templateId, Long regionId){
        Template template = this.templateReuseService.findTemplateById(PLATFORM, templateId);
        Optional<RegionTemplate> ret = this.regionTemplateRepository.retrieveByTemplateAndRegionId(template, regionId);
        //若没有与rid对应的地区模板，继续查找最近的上级地区模板
        if (ret.isEmpty()) {
            List<Region> pRegions = this.regionRepository.retrieveParentRegionsById(regionId);
            /*
             * 由近到远查询地区模板,只要找到一个不为空的地区模板就结束查询
             */
            for (Region r : pRegions) {
                ret = this.regionTemplateRepository.retrieveByTemplateAndRegionId(template, r.getId());
                if (ret.isPresent()) {
                    break;
                }
            }
        }
        if (ret.isPresent()) {
            RegionTemplate bo = ret.get();
            log.debug("findByTemplateIdAndRegionId: regionTemplate={}", bo);
            return bo;
        } else {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }
    /**
     * 获得有效的RegionIds
     * @param regionIds
     * @return
     */
    private List<Long> getValidRegionId(List<Long> regionIds) {
        return regionIds.stream().filter(id -> {
            try {
                Region region = this.regionRepository.findById(id);
                return !Region.ABANDONED.equals(region.getStatus());
            } catch (BusinessException exception) {
                if (ReturnNo.RESOURCE_ID_NOTEXIST.equals(exception.getErrno())) {
                    return false;
                }
            }
            return false;
        }).collect(Collectors.toUnmodifiableList());
    }

}
