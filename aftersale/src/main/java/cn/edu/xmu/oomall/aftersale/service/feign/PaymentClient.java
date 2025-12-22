package cn.edu.xmu.oomall.aftersale.service.feign;
import cn.edu.xmu.oomall.aftersale.controller.dto.RefundTransDto;
import cn.edu.xmu.oomall.aftersale.service.vo.PayTransVo;
import cn.edu.xmu.oomall.aftersale.service.vo.RefundTransVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
@FeignClient(name = "payment-module", url = "${payment-module.url}")
public interface PaymentClient {
    @GetMapping("shops/{shopId}/payments/{id}")
    InternalReturnObject<PayTransVo> findPaymentById(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long paymentId,
            @RequestHeader(value = "authorization", required = false) String token
    );

    @PutMapping("internal/shops/{shopId}/payments/{id}/refunds")
    InternalReturnObject<RefundTransVo> createRefund(
            @PathVariable("shopId") Long shopId,
            @PathVariable("id") Long paymentId,
            @RequestBody RefundTransDto refundTransDto,
            @RequestHeader(value = "authorization", required = false) String token
    );
}
