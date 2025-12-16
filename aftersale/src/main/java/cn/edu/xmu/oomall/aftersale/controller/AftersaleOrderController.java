package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.controller.dto.AuditAfterSalesDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.vo.AftersaleOrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.ReturnNo.*;


@RestController
@RequestMapping(value = "/shops/{shopId}/",produces = "application/json;charset=UTF-8")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AftersaleOrderController {
    private final AftersaleOrderService aftersaleOrderService;
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
        List<AftersaleOrder> aftersales = aftersaleOrderService.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize);
        List<AftersaleOrderVo> voList = aftersales.stream()
                .map(bo -> CloneFactory.copy(new AftersaleOrderVo(), bo))
                .collect(Collectors.toList());
        log.info("查询到 {} 条售后单，详情如下:", voList.size());
        voList.forEach(vo -> log.info("{}", vo));

        return new ReturnObject(voList);
    }

    /**
     * 审核售后单
     * @param shopId
     * @param id
     * @param dto
     * @param token
     * @return
     */
    @PutMapping("aftersales/{id}/confirm")
    public ReturnObject audit(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody AuditAfterSalesDTO dto,
            @RequestHeader(value = "authorization", required = false) String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            return new ReturnObject(AFTERSALE_NOT_LOGIN);
        }
        if (dto.getConfirm() == null) {
            return new ReturnObject(AFTERSALE_AUDIT_RESULT_EMPTY);
        }

        try {
            aftersaleOrderService.audit(
                    shopId,
                    id,
                    dto.getConfirm(),
                    dto.getConclusion(),
                    dto.getReason() // 把 reason 传进去
            );
            return new ReturnObject();

        } catch (IllegalArgumentException e) {
            return new ReturnObject(INTERNAL_SERVER_ERR);
        } catch (IllegalStateException e) {
            return new ReturnObject(INTERNAL_SERVER_ERR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ReturnObject(INTERNAL_SERVER_ERR);
        }
    }


}
