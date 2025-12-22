package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.*;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.edu.xmu.javaee.core.model.ReturnObject;

@Slf4j
@Component("expressAcceptAction")
public class ExpressAcceptAction implements AcceptAction {
    @Resource
    private ExpressClient expressClient;
    public Byte execute(ServiceOrder serviceOrder, UserToken user)
    {
        log.info("[FixAuditAction] 开始执行带创建运单的接受服务单逻辑，boId={},", serviceOrder.getId());

        // 1. 组装参数
        ExpressDto dto = new ExpressDto();
        /*
        *
        *sendRegionId(expressDto.getSender().getRegionId())
                .sendAddress(expressDto.getSender().getAddress())
                .sendMobile(expressDto.getSender().getMobile())
                .sendName(expressDto.getSender().getName())
                .receivRegionId(expressDto.getDelivery().getRegionId())
                .receivAddress(expressDto.getDelivery().getAddress())
                .receivMobile(expressDto.getDelivery().getMobile())
                .receivName(expressDto.getDelivery().getName())
                .contractId(expressDto.getShopLogisticId())
                .goodsType(expressDto.getGoodsType()).weight(expressDto.getWeight())
                .payMethod(expressDto.getPayMethod())
                .build();*/


        String token = null;

        try {
            // 2. 远程调用
            ReturnObject ret = expressClient.createPackage(

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
        return serviceOrder.UNCHECK;
    }
}
