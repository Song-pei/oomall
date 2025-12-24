package cn.edu.xmu.oomall.aftersale.mapper.po;
import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.vo.RefundTransVo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aftersale_refund")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CopyFrom(RefundTransVo.class)
public class RefundPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long aftersaleOrderId;
    private Long refundId;
    private Long amount;
    private Long divAmount;
    private Byte status;

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

}
