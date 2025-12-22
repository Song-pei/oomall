package cn.edu.xmu.oomall.aftersale.service.feign;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(name = "oomall-logistics")
public interface ExpressClient {

    // 物流的创建物流的InternalController的url感觉有问题???没有用internal
    @PostMapping("/shops/{shopId}/packages")
    InternalReturnObject<PackageResponseDTO> createPackage(
            @PathVariable("shopId") Long shopId,
            @RequestBody PackageCreateDTO packageCreateDTO,
            @RequestHeader("authorization") String token
    );
}