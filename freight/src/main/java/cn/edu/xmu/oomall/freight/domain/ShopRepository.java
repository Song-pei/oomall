package cn.edu.xmu.oomall.freight.domain;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.freight.domain.bo.Shop;
import cn.edu.xmu.oomall.freight.infrastructure.openfeign.ShopClient;
import cn.edu.xmu.oomall.freight.infrastructure.openfeign.po.ShopPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ShopRepository {

    private final ShopClient shopClient;

    public Shop findById(Long id) {
        InternalReturnObject<ShopPo> obj = this.shopClient.findById(id);
        return obj.getData();
    }
}
