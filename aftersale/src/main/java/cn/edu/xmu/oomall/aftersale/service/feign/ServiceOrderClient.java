package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCancelDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
@FeignClient(name = "service-module", url = "${service-module.url}")
public interface ServiceOrderClient {

    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    InternalReturnObject<ServiceOrderResponseDTO> createServiceOrder(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long afterSalesId,
            @RequestHeader(value = "authorization", required = false) String token,
            @RequestBody ServiceOrderCreateDTO payload
    );
    @PutMapping("/maintainers/{did}/service/{id}/cancel")
    InternalReturnObject<ServiceOrderResponseDTO> customerCancelServiceOrder(
            @PathVariable("did") Long did,
            @PathVariable("id") Long serviceOrderId,
            @RequestHeader(value = "authorization", required = false) String token,
            @RequestBody ServiceOrderCancelDTO payload
    );
}
