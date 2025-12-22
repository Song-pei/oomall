package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
public class AftersaleOrder extends OOMallObject implements Serializable{

    private Long shopId;
    private Long customerId;
    private Long orderId;
    private Long serviceOrderId;

    private Integer type; //0换货，1退货, 2维修
    private Integer status;
    private String conclusion;
    private String reason;

    private String customerName;
    private String customerMobile;
    private Long customerRegionId;
    private String customerAddress;

    private Byte inArbitrated;


    /** 待审核 */
    @JsonIgnore @ToString.Exclude public static final Integer UNAUDIT = 0;
    /** 待验收 */
    @JsonIgnore @ToString.Exclude public static final Integer UNCHECK = 1;
    /** 待换货 */
    @JsonIgnore @ToString.Exclude public static final Integer UNCHANGE = 2;
    /** 已生成服务单 */
    @JsonIgnore @ToString.Exclude public static final Integer GENERATE_SERVICEORDER = 3;
    /** 待退款 */
    @JsonIgnore @ToString.Exclude public static final Integer UNREFUND = 4;
    /** 取消 */
    @JsonIgnore @ToString.Exclude public static final Integer CANCEL = 5;
    /** 未接收 */
    @JsonIgnore @ToString.Exclude public static final Integer NOTACCEPT = 6;
    /** 已完成 */
    @JsonIgnore @ToString.Exclude public static final Integer FINISH = 7;

    /** 状态和名称的对应 */
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

    /** 允许的状态迁移 */
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
        put(UNCHANGE, new HashSet<>() {{ add(FINISH); }});
        put(GENERATE_SERVICEORDER, new HashSet<>() {{
            add(FINISH);
            add(CANCEL);
        }});
        put(UNREFUND, new HashSet<>() {{ add(FINISH); }});
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

    // =========================================================
    //                    核心业务逻辑
    // =========================================================

    /**
     * 填充审计信息（修改人、修改时间）
     * @param user 操作用户
     */
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

    /**
     * 1. 审核售后单
     * @param router 传入策略路由工具
     */
    public void audit(String conclusionIn, String reasonIn, boolean confirm, StrategyRouter router) {
        // 1. 基础状态校验
        if (!UNAUDIT.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许审核");
        }

        if (confirm) {
            // ============ 审核通过 ============
            this.conclusion = "同意";
            this.reason = null;

            // 2. 使用泛型 route 方法获取 AuditAction
            AuditAction action = router.route(this.type, this.status, "AUDIT", AuditAction.class);

            if (action == null) {
                log.error("未找到审核策略: type={}, status={}", this.type, this.status);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的审核策略");
            }

            // 3. 执行策略并获取目标状态
            Integer nextStatus = action.execute(this, conclusionIn);

            // 4.结合 allowStatus 校验状态流转是否合法
            if (nextStatus != null && this.allowStatus(nextStatus)) {
                this.status = nextStatus;
            } else {
                log.error("审核通过后试图流转到非法状态: current={}, next={}", this.status, nextStatus);
                throw new BusinessException(ReturnNo.STATENOTALLOW, "审核后状态流转异常");
            }

        } else {
            // ============ 审核拒绝 ============
            // 目标状态: NOTACCEPT (6)
            if (this.allowStatus(NOTACCEPT)) {
                this.status = NOTACCEPT;
                this.conclusion = "不同意";
                this.reason = reasonIn;
            } else {
                throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态无法拒绝");
            }
        }
    }


    /**
     * 2. 顾客取消售后单
     * @param router 传入策略路由工具
     */
    public void customerCancel(StrategyRouter router) {
        // 1. 获取取消策略
        CancelAction action = router.route(this.type, this.status, "CANCEL", CancelAction.class);

        Integer nextStatus = null;
        if (action != null) {
            // 执行策略
            nextStatus = action.execute(this);
        } else {
            // 如果没有策略，默认流转到 CANCEL 状态
            nextStatus = CANCEL;
        }

        // 2. 结合 allowStatus 校验状态流转是否合法
        if (nextStatus != null && this.allowStatus(nextStatus)) {
            this.status = nextStatus;
        } else {
            log.warn("尝试取消订单失败，状态流转不允许: id={}, current={}, target={}", this.id, this.status, nextStatus);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消");
        }
    }
}