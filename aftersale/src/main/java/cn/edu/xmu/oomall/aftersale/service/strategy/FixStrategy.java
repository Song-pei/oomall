package cn.edu.xmu.oomall.aftersale.service.strategy;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.TypeStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("2")
public class FixStrategy implements TypeStrategy {

    @Resource
    private ServiceOrderClient serviceOrderClient;

    @Override
    public void audit(AftersaleOrder bo, String conclusion) {
        log.info("[FixStrategy] 开始执行维修单策略，boId={}", bo.getId());

        // 1. 组装参数
        ServiceOrderCreateDTO dto = new ServiceOrderCreateDTO();
        dto.setType(0);
        ServiceOrderCreateDTO.Consignee consignee = new ServiceOrderCreateDTO.Consignee();
        consignee.setName(bo.getCustomerName());
        consignee.setMobile(bo.getCustomerMobile());
        consignee.setAddress(bo.getCustomerAddress());
        if (bo.getCustomerRegionId() != null) {
            consignee.setRegionId(bo.getCustomerRegionId().intValue());
        }
        dto.setConsignee(consignee);
        String token = null;

        try {
            serviceOrderClient.createServiceOrder(bo.getShopId(), bo.getId(), token, dto);
            log.info("[FixStrategy] 维修服务单创建成功");
        } catch (Exception e) {
            log.error("[FixStrategy] 创建服务单失败, boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }


    }
    public void accept(AftersaleOrder bo) {
        // 维修单不需要验收
    }
}
