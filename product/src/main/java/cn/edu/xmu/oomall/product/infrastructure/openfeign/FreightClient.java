package cn.edu.xmu.oomall.product.infrastructure.openfeign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.po.Freight;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
/**
 *
 * @author jyx
 */
@FeignClient("freight-service")
public interface FreightClient {

    @GetMapping("/shops/{shopId}/shoplogistics")
    InternalReturnObject<Freight> getAllLogisticsById(@PathVariable Long shopId,
                                                      @RequestParam(required = false, defaultValue = "1") Integer page,
                                                      @RequestParam(required = false, defaultValue = "10") Integer pageSize);
}
