//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.infrastructure.openfeign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.freight.infrastructure.openfeign.po.ShopPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "shop-service")
public interface ShopClient{

        @GetMapping("/shops/{id}")
        InternalReturnObject<ShopPo> findById(Long id);
}
