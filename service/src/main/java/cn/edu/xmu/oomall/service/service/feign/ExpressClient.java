package cn.edu.xmu.oomall.service.service.feign;

import cn.edu.xmu.javaee.core.aop.LoginUser;
import cn.edu.xmu.javaee.core.aop.UserLevel;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto; // 请求DTO
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo; // 返回DTO/PO
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "logistics-module", url = "${logistics-module.url}")
public interface ExpressClient {

    @PostMapping("/internal/shops/{shopId}/packages")
    InternalReturnObject<ExpressPo> createPackage(@PathVariable Long shopId,
                               @Validated @RequestBody ExpressDto expressDto,
                               @RequestHeader("user") String user,
                               @RequestHeader("userLevel") Integer userLevel) ;

    @PutMapping("/internal/shops/{shopId}/packages/{id}/cancel")
    InternalReturnObject<ExpressPo> cancelPackage(@PathVariable Long shopId,
                                      @PathVariable Long id,
                                      @RequestHeader("user") UserToken user,
                                      @RequestHeader("userLevel") Integer userLevel);



}
