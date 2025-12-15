//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.product.repository.onsale;

import cn.edu.xmu.oomall.product.model.OnSale;
import lombok.extern.slf4j.Slf4j;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

/**
 * 获得某个特定的onsale
 */
@Slf4j
public class SpecOnSaleExecutor implements OnSaleExecutor {

    private OnSaleRepository onsaleRepository;

    private Long onsaleId;

    public SpecOnSaleExecutor(OnSaleRepository onsaleRepository, Long onsaleId) {
        this.onsaleRepository = onsaleRepository;
        this.onsaleId = onsaleId;
    }

    @Override
    public OnSale execute() {
        log.debug("execute: onsaleId = {}", this.onsaleId);
        return this.onsaleRepository.findById(this.onsaleId).orElse(null);
    }
}
