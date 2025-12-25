package cn.edu.xmu.oomall.aftersale.mapper.po;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aftersale_aftersaleorder")
@Data
@NoArgsConstructor
@CopyFrom(AftersaleOrder.class)
public class AftersaleOrderPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shopId;
    private Long customerId;
    private Long orderId;
    private Long customerExpressId;
    private Long serviceOrderId;
    private Long refundId;
    private Long shopExpressId;

    private Integer type;
    private Integer serviceOrderType;
    private Integer status;
    private String conclusion;
    private String reason;
    private String exceptionDescription;//验收异常描述

    private String customerName;
    private String customerMobile;
    private Long customerRegionId;
    private String customerAddress;

    // --- 删除了 createTime 和 updateTime，因为数据库没这两列 ---

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Byte inArbitrated;
}