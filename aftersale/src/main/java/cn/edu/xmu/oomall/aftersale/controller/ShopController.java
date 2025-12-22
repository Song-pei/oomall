package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.controller.dto.AuditAfterSalesDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.InspectAftersalesDto;
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
public class ShopController {
    private final AftersaleOrderService aftersaleOrderService;


    /**
     * 查询售后单
     *
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
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      UserToken user) {

        log.debug("收到查询售后单请求: shopId={}, status={}, page={}, user={}", shopId, status, page, user);
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));

            user = new UserToken();
            user.setId(1L);
            user.setName("admin-test");
            user.setDepartId(0L);
        }


        List<AftersaleOrder> aftersales = aftersaleOrderService.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize, user);
        List<AftersaleOrderVo> voList = aftersales.stream()
                .map(bo -> CloneFactory.copy(new AftersaleOrderVo(), bo))
                .collect(Collectors.toList());
        log.info("查询到 {} 条售后单，详情如下:", voList.size());
        voList.forEach(vo -> log.info("{}", vo));

        return new ReturnObject(voList);
    }

    /**
     * 审核售后单
     *
     * @param shopId
     * @param id
     * @param dto
     * @param user
     * @return
     */
    @PutMapping("aftersales/{id}/confirm")
    public ReturnObject audit(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody AuditAfterSalesDTO dto,
            UserToken user) {

        log.info("收到审核请求: shopId={}, id={}, result={}, user={}", shopId, id, dto.getConfirm(), user);
        // 如果没有登录（或者测试环境下），手动创建一个模拟的管理员用户
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));
            user = new UserToken();
            user.setId(1L);
            user.setName("admin-test");
            user.setDepartId(0L);
        }


        if (dto.getConfirm() == null) {
            log.warn("审核失败：参数 confirm 为空, id={}", id);
            return new ReturnObject(AFTERSALE_AUDIT_RESULT_EMPTY);
        }

        try {
            aftersaleOrderService.audit(
                    shopId,
                    id,
                    dto.getConfirm(),
                    dto.getConclusion(),
                    dto.getReason(),
                    user
            );
            log.info("售后单审核成功: id={}", id);
            return new ReturnObject(ReturnNo.OK);
        } catch (IllegalArgumentException e) {
            log.warn("审核失败(参数错误): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.FIELD_NOTVALID);
        } catch (IllegalStateException e) {
            log.warn("审核失败(状态不允许): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.STATENOTALLOW);
        } catch (Exception e) {
            log.error("审核失败", e);
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    @PutMapping("aftersales/{id}/receive")
    public ReturnObject inspect(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestBody InspectAftersalesDto dto,
            UserToken user) {

        log.info("收到验收请求: shopId={}, id={}, confirm={}, user={}", shopId, id, dto.getConfirm(), user);
        // 如果没有登录（或者测试环境下），手动创建一个模拟的管理员用户
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));
            user = new UserToken();
            user.setId(1L);
            user.setName("admin-test");
            user.setDepartId(0L);
        }
        try {
            aftersaleOrderService.inspect(
                    shopId,
                    id,
                    dto.getExceptionDescription(),
                    dto.getConfirm(),
                    user
            );
            log.info("售后单验收成功: id={}", id);
            return new ReturnObject(ReturnNo.OK);
        } catch (IllegalArgumentException e) {
            log.warn("验收失败(参数错误): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.FIELD_NOTVALID);
        } catch (IllegalStateException e) {
            log.warn("验收失败(状态不允许): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.STATENOTALLOW);
        } catch (Exception e) {
            log.error("验收失败", e);
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }
}