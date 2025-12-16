package cn.edu.xmu.oomall.aftersale.dao.bo;

import ch.qos.logback.classic.Logger;
import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.strategy.AuditStrategyFactory;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.AuditStrategy;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
@Slf4j
//@CopyNotNullTo(Aftersale.class)
@Data
@NoArgsConstructor
@CopyFrom(AftersaleOrderPo.class)
public class AftersaleOrder {
    private Long id;
    private Long shopId;
    private Long customerId;
    private Long orderId;
    private Integer type;
    private Integer status;
    private String conclusion;
    private String reason;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Byte inArbitrated;

    public void audit(String conclusionIn, String reasonIn, boolean confirm) {
        this.updateTime = LocalDateTime.now();

        if (confirm) {
            // === 同意 ===
            this.status = 1;
            this.conclusion = "同意";
            this.reason = null; // 同意时清空理由

            // 执行策略
            AuditStrategy strategy = AuditStrategyFactory.getStrategy(this.type, this.conclusion);
            if (strategy != null) {
                strategy.execute(this, this.conclusion);
            }
        } else {
            // === 拒绝 ===
            this.status = 2;
            this.conclusion = "不同意";

            // 现在直接存入 reason 字段，不用拼接到 conclusion 了
            this.reason = reasonIn;
        }
    }
}
