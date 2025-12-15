package cn.edu.xmu.oomall.product.application;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.product.model.OnSale;
import cn.edu.xmu.oomall.product.model.Product;
import cn.edu.xmu.oomall.product.repository.ProductRepository;
import cn.edu.xmu.oomall.product.repository.onsale.OnSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReuseService {

    private final ProductRepository productRepository;
    private final OnSaleRepository onSaleRepository;

    public Product findNoOnsaleById(Long shopId, Long productId) {
        Product product=this.productRepository.findNoOnsaleById(productId).orElseThrow(() ->
                new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "产品", productId)));

        if (!Objects.equals(shopId, product.getShopId()) && !PLATFORM.equals(shopId)){
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "产品", productId, shopId));
        }
        return product;
    }

    public Product findValidById(Long shopId, Long productId){
        Product product = this.productRepository.findValidById(productId).orElseThrow(() ->
                new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "产品", productId)));


        if (!Objects.equals(shopId, product.getShopId()) && !PLATFORM.equals(shopId)){
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "产品", productId, shopId));
        }
        return product;
    }

    public Product findByOnsale(Long onsaleId){
        OnSale onsale = this.onSaleRepository.findById(onsaleId).orElseThrow(() ->
                new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "销售", onsaleId)));

        Product product = this.productRepository.findByOnsale(onsale).orElseThrow(() ->
                new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "产品", onsale.getProductId())));

        if (!Objects.equals(onsale.getShopId(), product.getShopId()) && !PLATFORM.equals(onsale.getShopId())){
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "产品", product.getId(), onsale.getShopId()));
        }
        return product;

    }

}
