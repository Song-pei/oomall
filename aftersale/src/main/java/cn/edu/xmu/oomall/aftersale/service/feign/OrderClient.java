package cn.edu.xmu.oomall.aftersale.service.feign;
import cn.edu.xmu.oomall.aftersale.controller.dto.OrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
@FeignClient(name = "prodorder-module", url = "${prodorder-module.url}")
public interface OrderClient {

    @GetMapping("internal/shops/{shopId}/orders/{id}")
    InternalReturnObject<OrderResponseDTO> findOrderById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long orderId,
            @RequestHeader(value = "authorization", required = false) String token
    );
}
