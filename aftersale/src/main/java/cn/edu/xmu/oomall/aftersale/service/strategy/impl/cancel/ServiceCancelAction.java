package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceFind;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCancelDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;

/**
 * 服务单取消策略
 * 场景：维修单审核通过后生成了“服务单”。
 */
@Slf4j
@Component("serviceCancelAction")
public class ServiceCancelAction implements CancelAction {

    @Resource
    private ServiceOrderClient serviceOrderClient;
    @Override
    public Integer execute(AftersaleOrder bo, UserToken user) {
        log.info("[ServiceCancelAction] 命中服务单取消策略，boId={}", bo.getId());
        // 必须存在服务单号
        if (bo.getServiceOrderId() == null) {
            throw new IllegalStateException("未找到关联服务单，无法取消");
        }



        // 获取 Token
        String token = user.getName();
        Long maintainId=null;
        /**
         * 此处需要调用服务模块的查找服务商Id的api,取消服务单需要传入服务商id,但售后表没有
         */
        try {
            // 远程调用
            InternalReturnObject<ServiceFind> ret = serviceOrderClient.getServiceOrder(
                    bo.getShopId(),
                    bo.getServiceOrderId(),
                    token
            );

            // 处理结果
            if (ret.getErrno() == 0) {
                maintainId=ret.getData().getMaintainer().getId();
                log.info("[ServiceCancelAction] 调取服务Find服务商, 服务单号: {}", bo.getServiceOrderId());
            } else {
                log.error("[ServiceCancelAction] 调取服务模块Find错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }
        } catch (BusinessException be) {
            throw be;
        }

        //  组装参数
        ServiceOrderCancelDTO dto = ServiceOrderCancelDTO.builder()
                .result("顾客取消服务单")
                .build();

        try {
            // 远程调用
            InternalReturnObject<ServiceOrderResponseDTO> ret = serviceOrderClient.customerCancelServiceOrder(
                    maintainId,//服务商id,需要先查
                    bo.getServiceOrderId(),
                    token,
                    dto
            );

            // 处理结果
            if (ret.getErrno() == 0) {
                log.info("[ServiceCancelAction] 服务单取消成功, 服务单号: {}", bo.getServiceOrderId());
            } else {
                log.error("[ServiceCancelAction] 服务模块取消错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (Exception e2) {
            log.error("[ServiceCancelAction] 远程调用取消异常, boId={}", bo.getId(), e2);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }



        return AftersaleOrder.CANCEL;
    }

}