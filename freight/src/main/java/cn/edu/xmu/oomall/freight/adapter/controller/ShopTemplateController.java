//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.adapter.controller;

import cn.edu.xmu.javaee.core.aop.Audit;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.javaee.core.model.PageDto;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.freight.adapter.controller.dto.PieceTemplateDto;
import cn.edu.xmu.oomall.freight.adapter.controller.dto.TemplateDto;
import cn.edu.xmu.oomall.freight.adapter.controller.dto.WeightTemplateDto;
import cn.edu.xmu.oomall.freight.application.TemplateReuseService;
import cn.edu.xmu.oomall.freight.application.vo.SimpleTemplateVo;
import cn.edu.xmu.oomall.freight.application.vo.TemplateVo;
import cn.edu.xmu.oomall.freight.domain.bo.template.*;
import cn.edu.xmu.oomall.freight.application.RegionTemplateService;
import cn.edu.xmu.oomall.freight.application.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/shops/{shopId}", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@Slf4j
public class ShopTemplateController {
    private final TemplateService templateService;
    private final RegionTemplateService regionTemplateService;

    /**
     * 管理员定义运费模板
     */
    @Audit(departName = "shops")
    @PostMapping("/templates")
    public ReturnObject createTemplate(
            @PathVariable("shopId") Long shopId,
            @Validated @RequestBody TemplateDto dto,
            @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user
    ) {
        Template template = CloneFactory.copy(new Template(), dto);
        if (dto.getType().equals(TemplateDto.WEIGHT)) {
            template.setTemplateBean(Template.WEIGHT);
        } else if (dto.getType().equals(TemplateDto.PIECE)) {
            template.setTemplateBean(Template.PIECE);
        }

        IdNameTypeVo ret = this.templateService.createTemplate(shopId, template, user);
        return new ReturnObject(ReturnNo.CREATED, ReturnNo.CREATED.getMessage(), ret);
    }

    /**
     * 获得商品的运费模板
     */
    @Audit(departName = "shops")
    @GetMapping("/templates")
    public ReturnObject retrieveTemplateByName(
            @PathVariable("shopId") Long shopId,
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        List<SimpleTemplateVo> templates = this.templateService.retrieveTemplateByName(shopId, name, page, pageSize);
        return new ReturnObject(new PageDto<>(templates, page, pageSize));
    }

    /**
     * 管理员克隆运费模板
     */
    @Audit(departName = "shops")
    @PostMapping("/templates/{id}/clone")
    public ReturnObject cloneTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user
    ) {
        IdNameTypeVo vo = regionTemplateService.cloneTemplate(id, shopId, user);

        return new ReturnObject(ReturnNo.CREATED, ReturnNo.CREATED.getMessage(), vo);

    }

    /**
     * 获得运费模板详情
     */
    @Audit(departName = "shops")
    @GetMapping("/templates/{id}")
    public ReturnObject findTemplateById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id
    ) {

        TemplateVo vo = this.templateService.findTemplateById(shopId, id);
        return new ReturnObject(vo);
    }

    /**
     * 管理员修改运费模板
     */
    @Audit(departName = "shops")
    @PutMapping("/templates/{id}")
    public ReturnObject updateTemplateById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @Validated @RequestBody TemplateDto vo,
            @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user
    ) {
        Template template = CloneFactory.copy(new Template(), vo);
        template.setId(id);
        templateService.updateTemplateById(shopId, template, user);
        return new ReturnObject(ReturnNo.OK);
    }

    /**
     * 删除运费模板，且同步删除与商品的关系
     */
    @Audit(departName = "shops")
    @DeleteMapping("/templates/{id}")
    public ReturnObject deleteTemplate(
            @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user,
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id
    ) {
        this.templateService.deleteTemplate(shopId, id);
        return new ReturnObject(ReturnNo.OK);
    }

    /**
     * 管理员定义重量模板明细
     */
    @Audit(departName = "shops")
    @PostMapping("/templates/{id}/weighttemplates")
    public ReturnObject createWeightTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @Validated @RequestBody WeightTemplateDto dto,
    ) {
        WeightTemplate bo = CloneFactory.copy(new WeightTemplate(), dto);
        bo.setTemplateId(id);
        this.regionTemplateService.insertRegionTemplate(shopId, bo, dto.getRegionIds(), Weight.class);
        return new ReturnObject(ReturnNo.CREATED);
    }

    /**
     * 管理员定义件数模板明细
     */
    @Audit(departName = "shops")
    @PostMapping("/templates/{id}/piecetemplates")
    public ReturnObject createPieceTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @Validated @RequestBody PieceTemplateDto dto,
            @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user
    ) {
        PieceTemplate bo = CloneFactory.copy(new PieceTemplate(), dto);
        bo.setTemplateId(id);
        this.regionTemplateService.insertRegionTemplate(shopId, bo, dto.getRegionIds(), Piece.class);
        return new ReturnObject(ReturnNo.CREATED);
    }

    /**
     * 管理员修改重量模板明细
     */
    @Audit(departName = "shops")
    @PutMapping("/weighttemplates/{id}")
    public ReturnObject updateWeightTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @Validated @RequestBody WeightTemplateDto dto
    ) {
        WeightTemplate bo = CloneFactory.copy(new WeightTemplate(), dto);
        bo.setId(id);
        this.regionTemplateService.updateRegionTemplate(shopId, bo, dto.getRegionIds(), Weight.class);
        return new ReturnObject();
    }

    /**
     * 管理员删除地区模板
     */
    @Audit(departName = "shops")
    @DeleteMapping("/regiontemplates/{rid}")
    public ReturnObject deleteRegionTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("rid") Long rid
    ) {
        regionTemplateService.deleteRegionTemplate(shopId, rid);
        return new ReturnObject(ReturnNo.OK);
    }


    /**
     * 管理员修改件数模板
     */
    @Audit(departName = "shops")
    @PutMapping("/piecetemplates/{id}")
    public ReturnObject updatePieceTemplate(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @Validated @RequestBody PieceTemplateDto dto
    ) {
        PieceTemplate bo = CloneFactory.copy(new PieceTemplate(), dto);
        bo.setId(id);
        this.regionTemplateService.updateRegionTemplate(shopId, bo,dto.getRegionIds(), Piece.class);
        return new ReturnObject();
    }

    /**
     * 店家或管理员查询运费模板明细
     */
    @Audit(departName = "shops")
    @GetMapping("/piecetemplates/{id}")
    public ReturnObject retrievePieceTemplateById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        /*
         * RegionTemplateVo是PieceTemplateVo和WeightTemplateVo的父类
         * 使用泛型自动映射
         * */
        List<RegionTemplate> ret = this.regionTemplateService.retrieveRegionTemplateById(shopId, id, page, pageSize);
        return new ReturnObject(new PageDto<>(ret, page, pageSize));
    }

    /**
     * 店家或管理员查询运费模板明细
     */
    @Audit(departName = "shops")
    @GetMapping("/weighttemplates/{id}")
    public ReturnObject retrieveWeightTemplateById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        /*
         * RegionTemplateVo是PieceTemplateVo和WeightTemplateVo的父类
         * 使用泛型自动映射
         * */
        List<RegionTemplate> ret = this.regionTemplateService.retrieveRegionTemplateById(shopId, id, page, pageSize);
        return new ReturnObject(new PageDto<>(ret, page, pageSize));
    }

}
