//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;

import java.io.Serializable;

public class Piece extends TemplateType implements Serializable {

    @Override
    public Integer getCount(ProductItem item) {
        return 1;
    }
}
