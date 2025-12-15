//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.adapter.controller;

import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.oomall.freight.adapter.controller.dto.ProductItemDto;
import cn.edu.xmu.oomall.freight.application.RegionTemplateService;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class InternalFreightController {
    private final RegionTemplateService regionTemplateService;
    /**
     * 计算一批商品的运费并根据策略分包
     *
     * @param tid
     * @param rid
     * @param dtoList
     * @return
     */
    @PostMapping("/internal/templates/{id}/regions/{rid}/freightprice")
    public ReturnObject getFreight(
            @PathVariable("id") Long tid,
            @PathVariable("rid") Long rid,
            @Validated @RequestBody List<ProductItemDto> dtoList
    ) {
        List<ProductItem> boList = dtoList.stream().map(productItemDto -> CloneFactory.copy(new ProductItem(), productItemDto)).collect(Collectors.toList());
        return new ReturnObject(this.regionTemplateService.cacuFreightPrice(boList, tid, rid));
    }
}
