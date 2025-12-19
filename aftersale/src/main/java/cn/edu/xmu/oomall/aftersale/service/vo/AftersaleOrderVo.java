package cn.edu.xmu.oomall.aftersale.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data // 1. 使用 @Data 自动生成所有 Getter/Setter/ToString/Equals
@CopyFrom(AftersaleOrder.class)
public class AftersaleOrderVo {

    private Long id;
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

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;

    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    private Byte inArbitrated;
}