package cn.edu.xmu.oomall.aftersale.service.strategy.impl.inspect;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.controller.dto.OrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PayTransDto;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.OrderClient;
import cn.edu.xmu.oomall.aftersale.service.feign.PaymentClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.InspectAction;
import cn.edu.xmu.oomall.aftersale.service.vo.PayTransVo;
import cn.edu.xmu.oomall.aftersale.service.vo.RefundTransVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("refundInspectAction")
public class RefundInspectAction implements InspectAction {
    @Resource
    private OrderClient orderClient;
    @Resource
    private PaymentClient paymentClient;

    @Override
    public Integer execute(Long shopId,AftersaleOrder bo) {
        log.info("[RefundInspectAction]开始执行退款验收通过逻辑: aftersaleId={}", bo.getId());

        try{
            // 调用订单服务，获取支付单id
            OrderResponseDTO orderResponseDTO= orderClient.findOrderById(shopId,bo.getOrderId()).getData();

            if(orderResponseDTO==null){
                log.error("[RefundInspectAction] 未找到订单: orderId={}", bo.getOrderId());
                throw new RuntimeException("未找到订单");
            }

            // 获取支付单id
            Long paymentId=orderResponseDTO.getPaymentId();

            // 调用支付服务，获取支付单
            PayTransVo payTransVo=paymentClient.findPaymentById(shopId,paymentId).getData();

            if(payTransVo==null){
                log.error("[RefundInspectAction] 未找到支付单: paymentId={}", paymentId);
                throw new RuntimeException("未找到支付单");
            }

            // 组装参数
            PayTransDto paytransDto= CloneFactory.copy(new PayTransDto(), payTransVo);

            // 调用支付服务，创建退款单
            RefundTransVo refundTransVo=paymentClient.createRefund(shopId,payTransVo.getId(),paytransDto).getData();

            if(refundTransVo==null){
                log.error("[RefundInspectAction] 退款单创建失败: paymentId={}", paymentId);
                throw new RuntimeException("退款单创建失败");
            }

            log.info("[RefundInspectAction] 退款单创建成功: aftersaleId={}, refundId={}", bo.getId(), refundTransVo.getId());
        }catch (Exception e){

            log.error("[RefundInspectAction] 远程调用异常, aftersaleId={}", bo.getId(), e);
            throw new RuntimeException("远程调用异常");
        }
        return AftersaleOrder.UNREFUND;
    }
}
