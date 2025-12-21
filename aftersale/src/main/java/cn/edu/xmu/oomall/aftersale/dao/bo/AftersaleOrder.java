package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.Strategy;
import cn.edu.xmu.oomall.aftersale.service.strategy.enums.AfterSalesStatus; // 导入枚举
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@CopyFrom(AftersaleOrderPo.class)
public class AftersaleOrder extends OOMallObject {

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
                    this.status = AfterSalesStatus.WAIT_FOR_INSPECTION.getCode();
                }
            }
        } else {
            // ============ 审核拒绝 ============
            // 使用枚举，状态 7
            this.status = AfterSalesStatus.REJECTED.getCode();
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
            // 修正：原代码写的是 8 (已完成)，根据枚举应该是 6 (取消)
            this.status = AfterSalesStatus.CANCELLED.getCode();
        }
    }
}