//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.product.repository.openfeign;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.ShopClient;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.po.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TemplateRepository {

    private final ShopClient shopClient;

    public Template findById(Long shopId, Long id) {
        InternalReturnObject<Template> ret = this.shopClient.getTemplateById(shopId, id);
        return ret.getData();
    }

}
