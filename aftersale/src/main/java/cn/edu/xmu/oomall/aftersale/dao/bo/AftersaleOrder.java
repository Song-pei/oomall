package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.Strategy;
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
     */
    public void audit(String conclusionIn, String reasonIn, boolean confirm, Strategy strategy) {
        this.setGmtModified(LocalDateTime.now());

        if (confirm) {
            // 同意
            this.conclusion = "同意";
            this.reason = null;

            // 调用 Strategy 里的 audit 方法
            if (strategy != null) {
                strategy.audit(this, conclusionIn);
            }
            this.status = 1;
        } else {
            // 拒绝
            this.status = 7;
            this.conclusion = "不同意";
            this.reason = reasonIn;
        }
    }


    /**
     * 2. 取消服务单
     */
    public void cancel(Strategy strategy) {
        this.setGmtModified(LocalDateTime.now());

        // 1. 调用 Strategy 里的 cancel 方法 (执行外部操作，如释放库存)
        // 此时状态还是旧状态 (比如 0-待审核)
        if (strategy != null) {
            strategy.cancel(this);
        }

        // 2. 修改自身状态为 已取消 (假设是 8)
        this.status = 8;
    }
}