//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.product.repository.openfeign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.ShopClient;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.po.Shop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ShopRepository {
    private final ShopClient shopClient;

    /**
     * @modify Rui Li
     * @task 2023-dgn2-007
     */
    public Shop findById(Long id) {
        InternalReturnObject<Shop> ret = this.shopClient.getShopById(id);
        return ret.getData();
    }
}
