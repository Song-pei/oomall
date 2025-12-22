package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO; // 引入新的 ResponseDTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "logistics-module", url = "${logistics-module.url}")
public interface ExpressClient {

    @PutMapping("/shops/{shopId}/packages/{id}/cancel")
    InternalReturnObject<PackageResponseDTO> cancelPackage(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long expressId,
            @RequestHeader(value = "authorization", required = false) String token

    );
}