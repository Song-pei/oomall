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
@ToString(callSuper = true)      // 让 toString 包含父类字段
@EqualsAndHashCode(callSuper = true) // 让 equals 包含父类字段
@CopyFrom(AftersaleOrderPo.class)
public class AftersaleOrder extends OOMallObject { // 1. 继承基类

    // id, creatorId, gmtCreate 等字段全在父类里，这里统统删掉！

    private Long shopId;
    private Long customerId;
    private Long orderId;
    private Integer type;
    private Integer status;
    private String conclusion;
    private String reason;

    private String customerName;
    private String customerMobile;
    private Long customerRegionId;
    private String customerAddress;

    private Byte inArbitrated;

    // --- 删除了 createTime 和 updateTime ---

    // 必须实现父类的抽象方法
    @Override
    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public void audit(String conclusionIn, String reasonIn, boolean confirm, TypeStrategyFactory typeStrategyFactory) {
        // 2. 修正：更新标准的 gmtModified
        this.setGmtModified(LocalDateTime.now());

        if (confirm) {
            // === 同意 ===
            this.status = 1;
            this.conclusion = "同意";
            this.reason = null;

    //        AuditStrategyFactory auditStrategyFactory = new AuditStrategyFactory();
            TypeStrategy strategy = typeStrategyFactory.getStrategy(this.type);
//            AuditStrategy strategy = AuditStrategyFactory.getStrategy(this.type, this.conclusion);
            if (strategy != null) {
                strategy.audit(this, this.conclusion);
            }
        } else {
            // === 拒绝 ===
            this.status = 2;
            this.conclusion = "不同意";
            this.reason = reasonIn;
        }
    }
}