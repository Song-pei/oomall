package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
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
}
