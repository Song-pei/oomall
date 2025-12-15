//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.application;

import cn.edu.xmu.javaee.core.clonefactory.CloneFactory;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.freight.application.vo.SimpleTemplateVo;
import cn.edu.xmu.oomall.freight.application.vo.TemplateVo;
import cn.edu.xmu.oomall.freight.domain.ShopRepository;
import cn.edu.xmu.oomall.freight.domain.TemplateRepository;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import cn.edu.xmu.oomall.freight.domain.template.RegionTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;


@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class TemplateService {
    private final RedisUtil redisUtil;
    private final TemplateRepository templateRepository;
    private final RegionTemplateRepository regionTemplateRepository;
    private final ShopRepository shopRepository;
    private final TemplateReuseService templateReuseService;

    /**
     * 获得运费模板详情
     * @param shopId
     * @param id
     */
    public TemplateVo findTemplateById(Long shopId, Long id){
        Template template = this.templateReuseService.findTemplateById(shopId, id);
        return CloneFactory.copy(new TemplateVo(), template);
    }
    /**
     * 获得商品的运费模板
     * @author Zhanyu Liu
     * @date 2022/11/30 7:42
     * @param shopId 商铺id
     * @param name 模板名称
     * @param page
     * @param pageSize
     */
    public List<SimpleTemplateVo> retrieveTemplateByName(Long shopId, String name, Integer page, Integer pageSize){
        List<Template> templates = this.templateRepository.retrieveTemplateByName(shopId,name,page,pageSize);
        List<SimpleTemplateVo> voList = templates.stream().map(bo -> CloneFactory.copy(new SimpleTemplateVo(), bo)).collect(Collectors.toList());
        return voList;
    }

    /**
     * 管理员定义运费模板
     * @param shopId
     * @param template
     * @param user
     */
    public IdNameTypeVo createTemplate(Long shopId, Template template, UserToken user){
        this.shopRepository.findById(shopId);
        template.setShopId(shopId);
        Template newTemplate =  templateRepository.insert(template,user);
        return  IdNameTypeVo.builder().id(newTemplate.getId()).name(newTemplate.getName()).build();
    }


    /**
     * 管理员修改运费模板
     * @param shopId
     * @param template
     * @param user
     */

    public void updateTemplateById(Long shopId, Template template, UserToken user){
        this.templateReuseService.findTemplateById(shopId,template.getId());
        templateRepository.save(template,user);
    }

    /**
     * 删除运费模板
     * @param shopId 商铺id
     * @param id 运费模板id
     */
    public void deleteTemplate(Long shopId, Long id){
        Template template = this.templateReuseService.findTemplateById(shopId,id);
        List<String> delKeys = regionTemplateRepository.deleteTemplate(template);
        this.redisUtil.del(delKeys.toArray(new String[0]));
    }
}
