package cn.edu.xmu.oomall.service.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 服务单
 */
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@CopyFrom({ServiceOrder.class})
public class ServiceOrderVo {

    private Long id;
    private Byte type;
    private String consignee;
    private IdNameTypeVo region;
    private String address;
    private String mobile;
    private Byte status;
    private String maintainerName;
    private String maintainerMobile;
    private String description;
    private String result;
    private Long shopId;
    private Long expressId;
    private IdNameTypeVo maintainer;
    private IdNameTypeVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private IdNameTypeVo modifier;

public ServiceOrderVo(ServiceOrder serviceOrder) {
    super();
    if (java.util.Objects.isNull(serviceOrder)) {
        return;
    }

    // 1. 基础字段拷贝
    cn.edu.xmu.javaee.core.util.CloneFactory.copy(this, serviceOrder);

    this.shopId = serviceOrder.getShopId();
    this.expressId = serviceOrder.getExpressId();
    // 3. 嵌套对象的安全组装 (使用 IdNameTypeVo)
    // 每一个嵌套字段都要判断 ID 是否为空，防止 Builder 报错或 NPE
    if (null != serviceOrder.getRegionId()) {
        this.region = cn.edu.xmu.javaee.core.model.IdNameTypeVo.builder()
                .id(serviceOrder.getRegionId())
                .build();
    }

    if (null != serviceOrder.getMaintainerId()) {
        this.maintainer = cn.edu.xmu.javaee.core.model.IdNameTypeVo.builder()
                .id(serviceOrder.getMaintainerId())
                .name(serviceOrder.getMaintainerName())
                .build();
    }

    if (null != serviceOrder.getCreatorId()) {
        this.creator = cn.edu.xmu.javaee.core.model.IdNameTypeVo.builder()
                .id(serviceOrder.getCreatorId())
                .name(serviceOrder.getCreatorName())
                .build();
    }

    if (null != serviceOrder.getModifierId()) {
        this.modifier = cn.edu.xmu.javaee.core.model.IdNameTypeVo.builder()
                .id(serviceOrder.getModifierId())
                .name(serviceOrder.getModifierName())
                .build();
    }
}
}