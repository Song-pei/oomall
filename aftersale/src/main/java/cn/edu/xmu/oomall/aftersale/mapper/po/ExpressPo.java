package cn.edu.xmu.oomall.aftersale.mapper.po;
import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aftersale_express")
@Data
@NoArgsConstructor
public class ExpressPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long expressId;
    private String billCode;
    private Long aftersaleOrderId;

    private Integer direction;
    private Integer type;
    private Byte status;

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
