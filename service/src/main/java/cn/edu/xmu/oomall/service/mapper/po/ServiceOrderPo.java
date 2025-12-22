package cn.edu.xmu.oomall.service.mapper.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_service")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@CopyFrom({ServiceOrder.class})
public class ServiceOrderPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long maintainerId;
    private Long shopId;

    private String result;

    private Byte type;

    private String description;

    private Long regionId;
    private String address;

    private String consignee;
    private String mobile;

    private Byte status;

    private String maintainerMobile;
    private String maintainerName;

    private Long creatorId;
    private String creatorName;

    private Long modifierId;
    private String modifierName;

    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    private Long productId;
    private String serialNo;
    private String expressId;
}
