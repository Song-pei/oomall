//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.product.repository.onsale;

import cn.edu.xmu.oomall.product.model.OnSale;
import lombok.extern.slf4j.Slf4j;

/**
 * 获得当前有效的onsale
 */
@Slf4j
public class ValidOnSaleExecutor implements OnSaleExecutor {


    private OnSaleRepository onsaleRepository;

    private Long productId;

    public ValidOnSaleExecutor(OnSaleRepository onsaleRepository, Long productId) {
        this.onsaleRepository = onsaleRepository;
        this.productId = productId;
    }

    @Override
    public OnSale execute() {
        log.debug("execute: productId = {}", this.productId);
        return this.onsaleRepository.findLatestValidOnsaleByProductId(this.productId).orElse(null);
    }
}
