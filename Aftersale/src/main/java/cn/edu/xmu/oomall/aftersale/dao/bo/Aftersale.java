package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersalePo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@CopyFrom(AftersalePo.class)
//@CopyNotNullTo(Aftersale.class)
@Data
@NoArgsConstructor
public class Aftersale {
    private Long id;
    private Long orderItemId;
    private Long customerId;
    private Long shopId;
    private String aftersaleSn;
    private Byte type;
    private String reason;
    private String conclusion;
    private Long quantity;
    private Long regionId;
    private String address;
    private String consignee;
    private String mobile;
    private Byte status;
    private Long serviceId;
    private String serialNo;
    private String name;
    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private Byte inArbitrated;
}
