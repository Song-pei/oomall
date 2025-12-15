//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;


public class Weight extends TemplateType {



    public Integer getCount(ProductItem item) {
        return item.getWeight().intValue();
    }

}
