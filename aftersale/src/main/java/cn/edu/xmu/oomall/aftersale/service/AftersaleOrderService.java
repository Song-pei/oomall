package cn.edu.xmu.oomall.aftersale.service;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class AftersaleOrderService {

    private final StrategyRouter strategyRouter;
    private final AftersaleOrderDao aftersaleOrderDao;

    /**
     * 分页查询所有售后单
     */
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize, UserToken user) {
        return aftersaleOrderDao.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize, user);
    }


    /**
     * 根据ID查询售后单详情
     */
    public AftersaleOrder getAftersaleById(Long shopId, Long id, UserToken user) {
        // 1. 调用 DAO 查询 PO 对象
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);

        // 2. 校验是否存在
        if (po == null) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        // 3. 校验 path 中的 shopId 是否与数据中的 shopId 一致
        // 防止用户通过 A 店铺的 API 访问 B 店铺的数据
        if (!po.getShopId().equals(shopId)) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "该店铺下无此售后单");
        }

        // 4. 权限隔离校验
        if (user.getDepartId() != null && user.getDepartId() != 0L) {
            if (!user.getDepartId().equals(po.getShopId())) {
                throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权查看其他店铺的售后单");
            }
        }

        // 5. PO 转 BO 并返回
        return CloneFactory.copy(new AftersaleOrder(), po);
    }

    /**
     * 审核售后单
     */
    public void audit(Long shopId, Long id,
                      Boolean confirm,
                      String conclusion,
                      String reason,
                      UserToken user) {

        log.info("开始审核售后单: shopId={}, id={}, confirm={}, user={}", shopId, id, confirm, user);

        // 1. 查出 PO
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            log.warn("审核失败: 售后单不存在, id={}", id);
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        // 2. 转为 BO
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);

        // 3. 校验
        if (!Objects.equals(bo.getShopId(), shopId)) {
            log.warn("审核失败: 店铺不匹配, id={}, shopId={}, targetShopId={}", id, bo.getShopId(), shopId);
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作该店铺订单");
        }

        if (!Objects.equals(bo.getStatus(), AftersaleOrder.UNAUDIT)) {
            log.warn("审核失败: 状态不正确, id={}, currentStatus={}", id, bo.getStatus());
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许审核");
        }

        // 4. 执行 BO 业务逻辑
        bo.audit(conclusion, reason, Boolean.TRUE.equals(confirm), strategyRouter);

        // 5. BO 更新审计信息
        bo.setModifier(user);

        // 6. 将 BO (包含状态变更和审计信息) 同步回 PO
        po = CloneFactory.copy(po, bo);

        // 7. 持久化
        aftersaleOrderDao.update(po);

        log.info("[ServiceFind] 审核完成: boId={}, 结果={}, reason={}", id, confirm, reason);
    }

    /**
     * 顾客取消售后单
     */
    public void customerCancel(Long id, UserToken user) {
        log.info("开始取消售后单: id={}, user={}", id, user);

        // 1. 查出 PO
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        // 2. 转为 BO
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        // 3. 校验 (请求中：待审核   处理中：待验收/已生成服务单
        if  (  !(Objects.equals(bo.getStatus(), AftersaleOrder.UNAUDIT))
             &&!(Objects.equals(bo.getStatus(), AftersaleOrder.UNCHECK))
             &&!(Objects.equals(bo.getStatus(), AftersaleOrder.GENERATE_SERVICEORDER))
        ) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消");
        }


        // 4. 执行 BO 业务逻辑
        bo.customerCancel(strategyRouter,user);

        // 5.BO 更新审计信息
        bo.setModifier(user);

        // 6. 将 BO 同步回 PO
        po = CloneFactory.copy(po, bo);

        // 7. 持久化
        aftersaleOrderDao.update(po);

        log.info("[ServiceFind] 取消完成: boId={}, 新状态={}", id, po.getStatus());
    }


    public void inspect(Long shopId ,Long id, String exceptionDescription, boolean confirm, UserToken user) {
        log.info("开始验收售后单: shopId={}, id={}, confirm={}, user={}", shopId, id, confirm, user);

        // 1. 查出 PO
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            log.warn("验收失败: 售后单不存在, id={}", id);
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        // 2. 转为 BO
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);

        // 3. 校验
        if (!Objects.equals(bo.getShopId(), shopId)) {
            log.warn("验收失败: 店铺不匹配, id={}, shopId={}, targetShopId={}", id, bo.getShopId(), shopId);
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作该店铺订单");
        }

        // 4. 执行 BO 业务逻辑
        bo.inspect(exceptionDescription, confirm, strategyRouter, user);

        // 5. BO 更新审计信息
        bo.setModifier(user);

        // 6. 将 BO (包含状态变更和审计信息) 同步回 PO
        po = CloneFactory.copy(po, bo);

        // 7. 持久化
        aftersaleOrderDao.update(po);

        log.info("[Service] 验收完成: boId={}, 结果={}", id, confirm);
    }
}