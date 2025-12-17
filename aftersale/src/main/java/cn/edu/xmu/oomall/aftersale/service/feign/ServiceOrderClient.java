package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "service-module", url = "${service-module.url}")
public interface ServiceOrderClient {

    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    void createServiceOrder(
            // 对应 URL 里的 {shopId}
            @PathVariable("shopId") Long shopId,

            // 对应 URL 里的 {id}
            @PathVariable("id") Long afterSalesId,

            @RequestHeader(value = "authorization", required = false) String token,
            @RequestBody ServiceOrderCreateDTO payload
    );
}
