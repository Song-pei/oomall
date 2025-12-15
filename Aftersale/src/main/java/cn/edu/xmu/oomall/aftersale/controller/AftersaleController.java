package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.bo.Aftersale;
import cn.edu.xmu.oomall.aftersale.service.AftersaleService;
import cn.edu.xmu.oomall.aftersale.service.vo.AftersaleVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/aftersale",produces = "application/json;charset=UTF-8")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AftersaleController {
    private final AftersaleService aftersaleService;
    /**
     * 查询售后单
     * @param shopId
     * @param aftersaleSn
     * @param orderSn
     * @param status
     * @param type
     * @param applyTime
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/aftersales")
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ReturnObject getAftersales(@RequestParam(required = false) Long shopId, @RequestParam(required = false) String aftersaleSn,
                                                            @RequestParam(required = false) String orderSn,
                                                            @RequestParam(required = false) Integer status, @RequestParam(required = false) Integer type,
                                                            @RequestParam(required = false) String applyTime, @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Aftersale> aftersales = aftersaleService.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize);
        List<AftersaleVo> voList = aftersales.stream()
                .map(bo -> CloneFactory.copy(new AftersaleVo(), bo))
                .collect(Collectors.toList());
        log.info("查询到 {} 条售后单，详情如下:", voList.size());
        voList.forEach(vo -> log.info("{}", vo));

        return new ReturnObject(voList);
    }

}
