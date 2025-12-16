package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "service-module", url = "${service-module.url}")
public interface ServiceOrderClient {

    /**
     * 创建服务单
     */
    @PostMapping("/internal/shops/{shopId}/aftersales/{id}/serviceorders")
    void createServiceOrder(@PathVariable("shopId") Long shopId,
                            @PathVariable("id") Long afterSalesId,
                            @RequestHeader("authorization") String token,
                            @RequestBody ServiceOrderCreateDTO payload);
}