package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.Strategy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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

    private Integer type;
    private Integer status;
    private String conclusion;
    private String reason;

    private String customerName;
    private String customerMobile;
    private Long customerRegionId;
    private String customerAddress;

    private Byte inArbitrated;


    /**
     * 待审核
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNAUDIT = 0;
    /**
     * 待验收
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNCHECK = 1;
    /**
     * 待换货
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNCHANGE = 2;
    /**
     * 已生成服务单
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer GENERATE_SERVICEORDER = 3;
    /**
     * 待退款
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer UNREFUND = 4;
    /**
     * 取消
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer CANCEL = 5;
    /**
     * 未接收
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer NOTACCEPT = 6;
    /**
     * 已完成
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Integer FINISH = 7;

    /**
     * 状态和名称的对应
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Map<Integer, String> STATUSNAMES = new HashMap() {
        {
            put(UNAUDIT, "待审核");
            put(UNCHECK, "待验收");
            put(UNCHANGE, "待换货");
            put(GENERATE_SERVICEORDER, "已生成服务单");
            put(UNREFUND, "待退款");
            put(CANCEL, "取消");
            put(NOTACCEPT, "未接受");
            put(FINISH, "已完成");
        }
    };
    /**
     * 允许的状态迁移
     */
    @JsonIgnore
    @ToString.Exclude
    private static final Map<Integer, Set<Integer>> toStatus = new HashMap<>() {
        {
            put(UNAUDIT, new HashSet<>() {
                {
                    add(UNCHECK);
                    add(GENERATE_SERVICEORDER);
                    add(CANCEL);
                    add(NOTACCEPT);
                }
            });
            put(UNCHECK, new HashSet<>() {
                {
                    add(UNCHANGE);
                    add(UNREFUND);
                    add(CANCEL);
                }
            });
            put(UNCHANGE, new HashSet<>() {
                {
                    add(FINISH);
                }
            });
            put(GENERATE_SERVICEORDER, new HashSet<>() {
                {
                    add(FINISH);
                    add(CANCEL);
                }
            });
            put(UNREFUND, new HashSet<>() {
                {
                    add(FINISH);

                }
            });

        }
    };
    /**
     * 是否允许状态迁移
     */
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
    /**
     * 获得当前状态名称
     */
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


    /**
     * 1. 审核服务单
     * @param strategy 传入的具体策略实现（由 StrategyRouter 获取）
     */
    public void audit(String conclusionIn, String reasonIn, boolean confirm, Strategy strategy) {
        this.setGmtModified(LocalDateTime.now());

        if (confirm) {
            // ============ 审核通过 ============
            this.conclusion = "同意";
            this.reason = null;

            if (strategy != null) {
                // 【核心修改】
                // 状态由 Strategy 决定，不再硬编码为 1
                // 维修策略返回 3 (已生成服务单)，退换策略返回 1 (待验收)
                Integer nextStatus = strategy.audit(this, conclusionIn);

                if (nextStatus != null) {
                    this.status = nextStatus;
                } else {
                    // 防御性逻辑：如果策略没返回状态，默认流转到待验收，避免数据异常
                    log.warn("Strategy audit returned null, fallback to WAIT_FOR_INSPECTION");
                    //现简单修改，后续可能需要使用allowStatus补充
                    this.status = UNCHECK;
                }
            }
        } else {
            // ============ 审核拒绝 ============
            //同上述的审核通过，应当经过 allowStatus 方法检查
            this.status = NOTACCEPT;
            this.conclusion = "不同意";
            this.reason = reasonIn;
        }
    }


    /**
     * 2. 取消服务单
     */
    public void cancel(Strategy strategy) {
        this.setGmtModified(LocalDateTime.now());

        Integer nextStatus = null;

        // 1. 调用 Strategy 执行外部操作 (如拦截物流)
        if (strategy != null) {
            // 【核心修改】接收策略返回的状态码
            nextStatus = strategy.cancel(this);
        }

        // 2. 修改自身状态
        if (nextStatus != null) {
            this.status = nextStatus;
        } else {
            // 同理，可能应当allowStatus 方法检查
            this.status = CANCEL;
        }
    }
}