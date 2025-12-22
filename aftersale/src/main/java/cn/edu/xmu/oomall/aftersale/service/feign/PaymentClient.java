package cn.edu.xmu.oomall.aftersale.service.feign;
import cn.edu.xmu.oomall.aftersale.controller.dto.PayTransDto;
import cn.edu.xmu.oomall.aftersale.service.vo.PayTransVo;
import cn.edu.xmu.oomall.aftersale.service.vo.RefundTransVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
@FeignClient(name = "payment-module", url = "${payment-module.url}")
public interface PaymentClient {
    @GetMapping("shops/{shopId}/payments/{id}")
    InternalReturnObject<PayTransVo> findPaymentById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long paymentId
    );

    @PutMapping("internal/shops/{shopId}/payments/{id}/refunds")
    InternalReturnObject<RefundTransVo> createRefund(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long paymentId,
            @RequestBody PayTransDto payTransDto
    );
}
