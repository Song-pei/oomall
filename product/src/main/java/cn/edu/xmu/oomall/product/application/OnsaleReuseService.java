package cn.edu.xmu.oomall.product.application;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.product.model.OnSale;
import cn.edu.xmu.oomall.product.repository.onsale.OnSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnsaleReuseService {

    private final OnSaleRepository onSaleRepository;

    public OnSale findById(Long shopId, Long onsaleId){
        OnSale onsale = this.onSaleRepository.findById(onsaleId)
                .orElseThrow(() -> new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "销售", onsaleId)));
        if (!shopId.equals(onsale.getShopId()) || !PLATFORM.equals(shopId)) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "销售",shopId));
        }
        return onsale;
    }
}
