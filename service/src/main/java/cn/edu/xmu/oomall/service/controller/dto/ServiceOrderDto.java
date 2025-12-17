package cn.edu.xmu.oomall.service.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.validation.NewGroup;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@CopyTo(ServiceOrder.class)
public class ServiceOrderDto {

    /**
     * 服务类型
     */
    @JsonProperty("type")
    @NotNull(message = "服务类型不能为空", groups = {NewGroup.class})
    private Byte type;

    /**
     * 收件人信息
     */
    @JsonProperty("consignee")
    @NotNull(message = "联系人信息不能为空", groups = {NewGroup.class})
    @Valid
    private ConsigneeDto consignee;
}