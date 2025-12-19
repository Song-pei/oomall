package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject; // 记得导入这个！
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.TypeStrategyFactory;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.TypeStrategy;
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
public class AftersaleOrder extends OOMallObject { // 1. 继承基类


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





    public void audit(String conclusionIn, String reasonIn, boolean confirm, TypeStrategyFactory typeStrategyFactory) {
        this.setGmtModified(LocalDateTime.now());

        if (confirm) {
            // === 同意 ===
            this.status = 1;
            this.conclusion = "同意";
            this.reason = null;

            TypeStrategy strategy = typeStrategyFactory.getStrategy(this.type);
            if (strategy != null) {
                strategy.audit(this, this.conclusion);
            }
        }
        else {
            // === 拒绝 ===
            this.status = 2;
            this.conclusion = "不同意";
            this.reason = reasonIn;
        }
    }
}