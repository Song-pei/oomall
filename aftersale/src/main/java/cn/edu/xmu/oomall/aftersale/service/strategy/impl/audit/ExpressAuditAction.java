package cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.enums.AfterSalesStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退换货审核动作
 * 适用场景：退货(0) 或 换货(1) 的待审核(0) 状态
 */
@Slf4j
@Component
public class ExpressAuditAction implements AuditAction {

    @Override
    public boolean supports(Integer type, Integer status) {
        // 核心筛选逻辑：
        // 1. 状态必须是 待审核(0)
        // 2. 类型必须是 退货(0) 或 换货(1)
        return status == 0 && (type == 0 || type == 1);
    }

    @Override
    public Integer execute(AftersaleOrder bo, String conclusion) {
        log.info("[ExpressAuditAction] 命中退换货审核策略，开始执行... boId={}, type={}", bo.getId(), bo.getType());

        // TODO: 在这里补充具体的业务逻辑

        // 审核通过后，状态变更为 待验收(1)
        return AfterSalesStatus.WAIT_FOR_INSPECTION.getCode();
    }
}