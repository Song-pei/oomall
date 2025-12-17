package cn.edu.xmu.oomall.service.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CopyFrom(ServiceOrder.class)
public class SimpleServiceVo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("type")
    private Byte type;

    @JsonProperty("consignee")
    private String consignee;
}