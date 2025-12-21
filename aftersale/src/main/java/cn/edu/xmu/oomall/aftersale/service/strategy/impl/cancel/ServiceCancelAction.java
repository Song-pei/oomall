package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.enums.AfterSalesStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 服务单取消策略
 * 场景：维修单审核通过后生成了“服务单”。
 */
@Slf4j
@Component
public class ServiceCancelAction implements CancelAction {

    @Override
    public boolean supports(Integer type, Integer status) {
        // 对应type=2(维修), status=3(已生成服务单)
        // 只有维修类型会进入状态 3
        return type == 2 && status == 3;
    }

    @Override
    public Integer execute(AftersaleOrder bo) {
        log.info("[ServiceCancelAction] 命中服务单取消策略，boId={}", bo.getId());
        // TODO: 1. 调用服务单微服务，取消对应的服务工单

        //取消状态变更为 已取消(6)
        return AfterSalesStatus.CANCELLED.getCode();
    }
}