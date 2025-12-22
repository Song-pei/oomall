package cn.edu.xmu.oomall.aftersale.service.strategy.impl.inspect;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.controller.dto.OrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.RefundTransDto;
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
            // 获取token
            String token=null;
            // 调用订单服务，获取支付单id
            InternalReturnObject<OrderResponseDTO> orderRet= orderClient.findOrderById(
                    shopId,
                    bo.getOrderId(),
                    token
            );

            if(orderRet.getErrno()==0 && orderRet.getData()!=null){
                log.error("[RefundInspectAction] 找到订单: orderId={}", bo.getOrderId());
            }
            else{
                log.error("[RefundInspectAction] 未找到订单: orderId={}", bo.getOrderId());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, orderRet.getErrmsg());
            }

            // 获取支付单id
            Long paymentId=orderRet.getData().getPaymentId();

            // 调用支付服务，获取支付单
            InternalReturnObject<PayTransVo> payTransRet=paymentClient.findPaymentById(
                    shopId,
                    paymentId,
                    token
            );

            if(payTransRet.getErrno()==0&& payTransRet.getData()!=null){
                log.error("[RefundInspectAction] 找到支付单: paymentId={}", paymentId);
            }
            else{
                log.error("[RefundInspectAction] 未找到支付单: paymentId={}", paymentId);
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, payTransRet.getErrmsg());
            }

            // 组装参数
            RefundTransDto refundTransDto= CloneFactory.copy(new RefundTransDto(), payTransRet.getData());

            // 调用支付服务，创建退款单
            InternalReturnObject<RefundTransVo> refundTransRet=paymentClient.createRefund(
                    shopId,
                    paymentId,
                    refundTransDto,
                    token
            );

            if(refundTransRet.getErrno()==0&& refundTransRet.getData()!=null){
                log.info("[RefundInspectAction] 退款单创建成功: aftersaleId={}, refundId={}", bo.getId(), refundTransRet.getData().getId());
            }
            else{
                log.error("[RefundInspectAction] 退款单创建失败: paymentId={}", paymentId);
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, refundTransRet.getErrmsg());
            }
            //bo.setRefundId(refundTransRet.getData().getId());

        }catch (BusinessException be){
            throw be;
        }
        catch (Exception e){
            log.error("[RefundInspectAction] 远程调用异常, aftersaleId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }
        return AftersaleOrder.UNREFUND;
    }
}
