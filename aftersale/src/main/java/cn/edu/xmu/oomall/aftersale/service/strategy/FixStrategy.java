package cn.edu.xmu.oomall.aftersale.service.strategy;

import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCreateDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.AuditStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FixStrategy implements AuditStrategy {

    @Resource
    private ServiceOrderClient serviceOrderClient;

    @Override
    public boolean match(Integer type, String conclusion) {
        // 只有类型是 2(维修) 且 conclusion是 "同意" 时才执行
        return Integer.valueOf(2).equals(type) && "同意".equals(conclusion);
    }

    @Override
    public void execute(AftersaleOrder bo, String conclusion) {
        log.info("[FixStrategy] 开始执行维修单策略，boId={}", bo.getId());

        // 1. 组装参数
        ServiceOrderCreateDTO dto = new ServiceOrderCreateDTO();
        dto.setType(0); // 0代表维修服务单

        ServiceOrderCreateDTO.Consignee consignee = new ServiceOrderCreateDTO.Consignee();

        // 使用 bo 里的 customerId 模拟名字
        consignee.setName("Customer_" + bo.getCustomerId());
        consignee.setMobile("13800000000");
        consignee.setAddress("Default Address");

        dto.setConsignee(consignee);

        String token = "Bearer internal_token";

        // 2. 远程调用
        try {
            serviceOrderClient.createServiceOrder(bo.getShopId(), bo.getId(), token, dto);
            log.info("[FixStrategy] 维修服务单创建成功");
        } catch (Exception e) {
            log.error("[FixStrategy] 创建服务单失败, boId={}", bo.getId(), e);
            throw new RuntimeException("远程调用服务模块失败", e);
        }
    }
}