package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.InspectAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyRouter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@CopyFrom(AftersaleOrderPo.class)
public class AftersaleOrder extends OOMallObject implements Serializable {

    private Long shopId;
    private Long customerId;
    private Long orderId;
    private Long serviceOrderId;
    private Long customerExpressId;
    private Long refundId;
    private Long shopExpressId;

    private Integer type; //0换货，1退货, 2维修
    private Integer status;
    private String conclusion;
    private String reason;
    private String exceptionDescription; //异常描述

    private String customerName;
    private String customerMobile;
    private Long customerRegionId;
    private String customerAddress;

    private Byte inArbitrated;

    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNAUDIT = 0;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNCHECK = 1;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNCHANGE = 2;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer GENERATE_SERVICEORDER = 3;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNREFUND = 4;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer CANCEL = 5;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer NOTACCEPT = 6;
    @JsonIgnore
    @ToString.Exclude
    public static final Integer FINISH = 7;

    @Setter
    @JsonIgnore
    private  AftersaleOrderDao dao;

    @JsonIgnore
    @ToString.Exclude
    public static final Map<Integer, String> STATUSNAMES = new HashMap() {{
        put(UNAUDIT, "待审核");
        put(UNCHECK, "待验收");
        put(UNCHANGE, "待换货");
        put(GENERATE_SERVICEORDER, "已生成服务单");
        put(UNREFUND, "待退款");
        put(CANCEL, "取消");
        put(NOTACCEPT, "未接受");
        put(FINISH, "已完成");
    }};

    @JsonIgnore
    @ToString.Exclude
    private static final Map<Integer, Set<Integer>> toStatus = new HashMap<>() {{
        put(UNAUDIT, new HashSet<>() {{
            add(UNCHECK);
            add(GENERATE_SERVICEORDER);
            add(CANCEL);
            add(NOTACCEPT);
        }});
        put(UNCHECK, new HashSet<>() {{
            add(UNCHANGE);
            add(UNREFUND);
            add(CANCEL);
        }});
        put(UNCHANGE, new HashSet<>() {{
            add(FINISH);
        }});
        put(GENERATE_SERVICEORDER, new HashSet<>() {{
            add(FINISH);
            add(CANCEL);
        }});
        put(UNREFUND, new HashSet<>() {{
            add(FINISH);
        }});
    }};

    public boolean allowStatus(Integer status) {
        boolean ret = false;
        if (null != status && null != this.status) {
            Set<Integer> allowStatusSet = toStatus.get(this.status);
            if (null != allowStatusSet) {
                ret = allowStatusSet.contains(status);
            }
        }
        return ret;
    }

    @JsonIgnore
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }

    @Override
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public void setModifier(UserToken user) {
        if (user != null) {
            this.setModifierId(user.getId());
            this.setModifierName(user.getName());
        } else {
            this.setModifierId(0L);
            this.setModifierName("System");
        }
        this.setGmtModified(LocalDateTime.now());
    }
   public void setAftersaleOrderDao(AftersaleOrderDao dao) {
        this.dao = dao;
   }
    /**
     * 审核售后单
     */
    public ActionResult<?> audit(String conclusionIn, String reasonIn, boolean confirm, StrategyRouter router, UserToken user) {
        if (!UNAUDIT.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许审核");
        }

        ActionResult<?> resultToReturn;

        if (!confirm) {
            if (this.allowStatus(NOTACCEPT)) {
                this.status = NOTACCEPT;
                this.conclusion = "不同意";
                this.reason = reasonIn;
                resultToReturn = ActionResult.status((Integer) NOTACCEPT);
            } else {
                throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态无法拒绝");
            }
        } else {
            this.conclusion = "同意";
            this.reason = null;

            AuditAction action = router.route(this.type, this.status, "AUDIT", AuditAction.class);

            if (action == null) {
                log.error("未找到审核策略: type={}, status={}", this.type, this.status);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的审核策略");
            }

            ActionResult<?> actionResult = action.execute(this, conclusionIn);

            Integer nextStatus = null;
            if (actionResult != null && actionResult.getNextStatus() != null) {
                nextStatus = actionResult.getNextStatus().intValue();
            }

            if (nextStatus != null && this.allowStatus(nextStatus)) {
                this.status = nextStatus;
            } else {
                log.error("审核通过后试图流转到非法状态: current={}, next={}", this.status, nextStatus);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "审核后状态流转异常");
            }
            resultToReturn = actionResult;
        }

        this.setModifier(user);
        AftersaleOrderPo po = CloneFactory.copy(new AftersaleOrderPo(), this);
        // 调用dao层
        dao.update(po);

        return resultToReturn;
    }


    /**
     * 顾客取消售后单
     */
    public ActionResult<?> customerCancel(StrategyRouter router, UserToken user) {
        CancelAction action = router.route(this.type, this.status, "CANCEL", CancelAction.class);

        if (action == null) {
            log.warn("未找到取消策略: type={}, status={}", this.type, this.status);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前业务类型不支持取消操作");
        }

        ActionResult<?> actionResult = action.execute(this, user);

        Integer nextStatus = null;
        if (actionResult != null && actionResult.getNextStatus() != null) {
            nextStatus = actionResult.getNextStatus().intValue();
        }

        if (nextStatus != null && this.allowStatus(nextStatus)) {
            this.status = nextStatus;
        } else {
            log.warn("尝试取消订单失败，状态流转不允许: id={}, current={}, target={}", this.id, this.status, nextStatus);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消");
        }

        this.setModifier(user);
        AftersaleOrderPo po = CloneFactory.copy(new AftersaleOrderPo(), this);
        // 调用dao层
        dao.update(po);

        return actionResult;
    }

    /**
     * 验收售后单
     */
    public ActionResult<?> inspect(String exceptionDescription, boolean confirm, StrategyRouter router, UserToken user) {
        if (!UNCHECK.equals(this.status)) {
            log.info("当前状态不允许验收: id={}, status={}", this.id, this.status);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许验收");
        }

        ActionResult<?> resultToReturn;

        if (!confirm) {
            this.exceptionDescription = exceptionDescription;
            InspectAction refuseAction = router.route(this.type, this.status, "REFUSEINSPECT", InspectAction.class);
            if (refuseAction == null) {
                log.error("未找到验收拒绝策略: type={}, status={}", this.type, this.status);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的验收拒绝策略");
            }

            ActionResult<?> refuseResult = refuseAction.execute(this, user);

            Integer refuseNextStatus = (refuseResult != null && refuseResult.getNextStatus() != null)
                    ? refuseResult.getNextStatus().intValue() : null;

            if (refuseNextStatus != null && this.allowStatus(refuseNextStatus)) {
                this.status = refuseNextStatus;
            } else {
                log.error("验收拒绝后试图流转到非法状态: current={}, next={}", this.status, refuseNextStatus);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "验收拒绝状态流转异常");
            }
            resultToReturn = refuseResult;
        } else {
            this.exceptionDescription = null;

            InspectAction action = router.route(this.type, this.status, "INSPECT", InspectAction.class);

            if (action == null) {
                log.error("未找到验收通过策略: type={}, status={}", this.type, this.status);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的验收通过策略");
            }

            ActionResult<?> actionResult = action.execute(this, user);

            Integer nextStatus = (actionResult != null && actionResult.getNextStatus() != null)
                    ? actionResult.getNextStatus().intValue() : null;

            if (nextStatus != null && this.allowStatus(nextStatus)) {
                this.status = nextStatus;
            } else {
                log.error("验收通过后试图流转到非法状态: current={}, next={}", this.status, nextStatus);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "验收通过状态流转异常");
            }
            resultToReturn = actionResult;
        }

        this.setModifier(user);
        AftersaleOrderPo po = CloneFactory.copy(new AftersaleOrderPo(), this);
        // 调用dao层
        dao.update(po);

        return resultToReturn;
    }
}