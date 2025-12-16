package cn.edu.xmu.oomall.aftersale.mapper.po;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;

@Entity
@Table(name = "after_sales_order")
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
}
