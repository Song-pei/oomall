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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class AftersaleOrderService {

    private final StrategyRouter strategyRouter;
    @Setter
    private final AftersaleOrderDao aftersaleOrderDao;


    /**
     * 分页搜索售后单列表
     */
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize, UserToken user) {
        return aftersaleOrderDao.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize, user);
    }

    /**
     * 管理员根据ID查询售后单详情
     */
    public AftersaleOrder getAftersaleById(Long shopId, Long id, UserToken user) {
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);

        if (po == null) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        if (!po.getShopId().equals(shopId)) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "该店铺下无此售后单");
        }

        if (user.getDepartId() != null && user.getDepartId() != 0L) {
            if (!user.getDepartId().equals(po.getShopId())) {
                throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权查看其他店铺的售后单");
            }
        }
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        //aftersaleOrderDao.build(bo);
        bo.setAftersaleOrderDao(aftersaleOrderDao);
        return bo;
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

        // 调用dao层
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            log.warn("审核失败: 售后单不存在, id={}", id);
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        //aftersaleOrderDao.build(bo);
        bo.setAftersaleOrderDao(aftersaleOrderDao);

        if (!Objects.equals(bo.getShopId(), shopId)) {
            log.warn("审核失败: 店铺不匹配, id={}, shopId={}, targetShopId={}", id, bo.getShopId(), shopId);
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作该店铺订单");
        }

        if (!Objects.equals(bo.getStatus(), AftersaleOrder.UNAUDIT)) {
            log.warn("审核失败: 状态不正确, id={}, currentStatus={}", id, bo.getStatus());
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许审核");
        }

        bo.audit(conclusion, reason, Boolean.TRUE.equals(confirm), strategyRouter, user);

        log.info("[ServiceFind] 审核完成: boId={}, 结果={}, reason={}", id, confirm, reason);
    }

    /**
     * 取消售后单
     */
    public void customerCancel(Long id, UserToken user) {
        log.info("开始取消售后单: id={}, user={}", id, user);

        // 调用dao层
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        //aftersaleOrderDao.build(bo);
        bo.setAftersaleOrderDao(aftersaleOrderDao);
       /* if(!bo.CanBeCancel())
        {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消");
        }*/

        bo.customerCancel(strategyRouter, user);

        log.info("[ServiceFind] 取消完成: boId={}, 新状态={}", id, bo.getStatus());
    }

    /**
     * 顾客查询售后单详情
     */
    public  AftersaleOrder customerSearch(Long id,UserToken user)
    {
        // 调用dao层
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) { throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");}
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        //aftersaleOrderDao.build(bo);
        bo.setAftersaleOrderDao(aftersaleOrderDao);
        return bo;
    }

    /**
     * 验收售后单
     */
    public void inspect(Long shopId ,Long id, String exceptionDescription, boolean confirm, UserToken user) {
        log.info("开始验收售后单: shopId={}, id={}, confirm={}, user={}", shopId, id, confirm, user);

        // 调用dao层
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            log.warn("验收失败: 售后单不存在, id={}", id);
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在");
        }

        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        //aftersaleOrderDao.build(bo);
        bo.setAftersaleOrderDao(aftersaleOrderDao);
        if (!Objects.equals(bo.getShopId(), shopId)) {
            log.warn("验收失败: 店铺不匹配, id={}, shopId={}, targetShopId={}", id, bo.getShopId(), shopId);
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作该店铺订单");
        }

        bo.inspect(exceptionDescription, confirm, strategyRouter, user);

        log.info("[Service] 验收完成: boId={}, 结果={}", id, confirm);
    }
}