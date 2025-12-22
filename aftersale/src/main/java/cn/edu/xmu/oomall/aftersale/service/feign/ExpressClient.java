package cn.edu.xmu.oomall.aftersale.service.feign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO; // 引入新的 ResponseDTO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "logistics-module", url = "${logistics-module.url}")
public interface ExpressClient {

    @PostMapping("/internal/shops/{shopId}/packages")
    InternalReturnObject<PackageResponseDTO> createPackage(
            @PathVariable("shopId") Long shopId,
            @RequestBody PackageCreateDTO expressDto,
            @RequestHeader(value = "authorization", required = false) String token
    );
}