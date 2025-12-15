//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.product.repository.onsale;

import cn.edu.xmu.oomall.product.model.OnSale;


/**
 * 获得某个特定的onsale
 */
public class NoOnSaleExecutor implements OnSaleExecutor {

    public NoOnSaleExecutor() {
    }

    @Override
    public OnSale execute() {
        return OnSale.builder().id(OnSale.NOTEXIST).build();
    }
}
