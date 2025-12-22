package cn.edu.xmu.oomall.aftersale.service.feign;
import cn.edu.xmu.javaee.core.aop.UserLevel;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "oomall-logistics",url = "${logistics-module.url}")
public interface ExpressClient {

    // 物流的创建物流的InternalController的url感觉有问题???没有用internal
    @PostMapping("/shops/{shopId}/packages")
    InternalReturnObject<PackageResponseDTO> createPackage(
            @PathVariable("shopId") Long shopId,
            @RequestBody PackageCreateDTO packageCreateDTO,
            @RequestHeader("authorization") String token
    );
    @PutMapping("/shops/{shopId}/packages/{id}/cancel")
    InternalReturnObject<PackageResponseDTO> cancelPackage(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long expressId,
            @RequestHeader(value = "authorization", required = false)  String token


    );
}