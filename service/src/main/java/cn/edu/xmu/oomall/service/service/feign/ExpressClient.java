package cn.edu.xmu.oomall.service.service.feign;

import cn.edu.xmu.javaee.core.aop.UserLevel;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;

@FeignClient(name = "logistics-module", url = "${logistics-module.url}")
public interface ExpressClient {

    @PostMapping("/internal/shops/{shopId}/packages")
    ReturnObject createPackage(@PathVariable Long shopId,
                               @Validated @RequestBody ExpressDto expressDto,
                               @cn.edu.xmu.javaee.core.aop.LoginUser UserToken user,
                               @UserLevel Integer userLevel) ;
}

