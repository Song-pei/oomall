//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
public class TemplateResult {

    /**
     * 费用
     */
    private Long fee;
    /**
     *  包括的商品
     */
    private Collection<ProductItem> pack;

}
