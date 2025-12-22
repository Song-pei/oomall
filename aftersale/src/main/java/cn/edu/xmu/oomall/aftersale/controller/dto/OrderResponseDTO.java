package cn.edu.xmu.oomall.aftersale.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderResponseDTO {
    @NotNull(message = "订单id不能为空")
    private Long orderId;
    @NotNull(message = "支付单id不能为空")
    private Long paymentId;
}
