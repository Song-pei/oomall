package cn.edu.xmu.oomall.aftersale.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.validation.NewGroup;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AftersaleDto {

    @NotNull(message = "订单ID不能为空", groups = {NewGroup.class})
    private Long orderId;

    @NotBlank(message = "原因不能为空", groups = {NewGroup.class})
    private String reason;
}

