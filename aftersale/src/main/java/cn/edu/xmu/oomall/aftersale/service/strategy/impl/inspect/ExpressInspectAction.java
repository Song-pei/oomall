package cn.edu.xmu.oomall.aftersale.service.strategy.impl.inspect;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.InspectAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("expressInspectAction")
public class ExpressInspectAction implements InspectAction {
    @Override
    public Integer execute(Long shopId, AftersaleOrder aftersaleOrder) {
        log.info("[ExpressInspectAction] 开始执行退换货验收策略 aftersaleId={}", aftersaleOrder.getId());
        return AftersaleOrder.UNCHANGE;

    }
}