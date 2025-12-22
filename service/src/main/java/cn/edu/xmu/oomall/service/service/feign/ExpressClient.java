package cn.edu.xmu.oomall.service.service.feign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto; // 请求DTO
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo; // 返回DTO/PO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "logistics-module", url = "${logistics-module.url}")
public interface ExpressClient {

    @PostMapping("/internal/shops/{shopId}/packages")
    InternalReturnObject<ExpressPo> createPackage(
            @PathVariable("shopId") Long shopId,
            @RequestBody ExpressDto expressDto,
            @RequestHeader(value = "authorization", required = false) String token,
            @RequestHeader(value = "userLevel", required = false) Integer userLevel
    );
}
