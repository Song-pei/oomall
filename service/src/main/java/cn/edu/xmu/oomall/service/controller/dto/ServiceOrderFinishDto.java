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
public class ServiceOrderFinishDto {

    @JsonProperty("result")
    @NotBlank(message = "服务结果描述不能为空", groups = {NewGroup.class})
    private String result;
}