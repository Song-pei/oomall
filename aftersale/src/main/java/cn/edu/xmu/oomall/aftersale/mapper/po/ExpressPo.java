package cn.edu.xmu.oomall.aftersale.mapper.po;
import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.dao.bo.Express;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "aftersale_express")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CopyFrom(PackageResponseDTO.class)
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
