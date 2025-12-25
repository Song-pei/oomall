package cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 维修审核动作
 * 场景：维修单(Type=2) 审核通过时，需要调用第三方服务创建工单
 */
@Slf4j
@Component("fixAuditAction")
public class FixAuditAction implements AuditAction {

    @Resource
    private ServiceOrderClient serviceOrderClient;
    @Override
    public <T> ActionResult<T> execute(AftersaleOrder bo, String conclusion) {
        log.info("[FixAuditAction] 开始执行维修单审核逻辑，boId={}, conclusion={}", bo.getId(), conclusion);

        // 1. 组装参数
        ServiceOrderCreateDTO dto = ServiceOrderCreateDTO.builder()
                .type(bo.getServiceOrderType()) // 服务模块定义的类型常量
                .consignee(ServiceOrderCreateDTO.Consignee.builder()
                        .name(bo.getCustomerName())
                        .mobile(bo.getCustomerMobile())
                        .address(bo.getCustomerAddress())
                        .regionId(bo.getCustomerRegionId() != null ? bo.getCustomerRegionId().intValue() : null)
                        .build())
                .build();

        String token = null;

        try {
            // 2. 远程调用
            InternalReturnObject<ServiceOrderResponseDTO> ret = serviceOrderClient.createServiceOrder(
                    bo.getShopId(),
                    bo.getId(),
                    token,
                    dto
            );

            // 3. 处理结果
            if (ret.getErrno() == 0 && ret.getData() != null) {
                Long serviceOrderId = ret.getData().getId();
                log.info("[FixAuditAction] 维修服务单创建成功, 服务单号: {}", serviceOrderId);
                bo.setServiceOrderId(serviceOrderId);
            } else {
                log.error("[FixAuditAction] 服务模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[FixAuditAction] 远程调用异常, boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }
        // 4. 返回新的状态：已生成服务单(3)
        return (ActionResult<T>) ActionResult.success(null, AftersaleOrder.GENERATE_SERVICEORDER);
    }

}