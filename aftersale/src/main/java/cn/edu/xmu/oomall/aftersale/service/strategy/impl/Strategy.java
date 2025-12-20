package cn.edu.xmu.oomall.aftersale.service.strategy.impl;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Strategy {

    // 注入所有的审核动作 (FixAuditAction, ExchangeAuditAction...)
    @Resource
    private List<AuditAction> auditActions;

    // 注入所有的取消动作 (FixCancelAction, ExchangeCancelAction...)
    @Resource
    private List<CancelAction> cancelActions;

    /**
     * 1. 审核遍历逻辑
     */
    public void audit(AftersaleOrder bo, String conclusion) {
        boolean handled = false;

        for (AuditAction action : auditActions) {
            if (action.supports(bo.getType(), bo.getStatus())) {
                log.debug("匹配到审核策略: {}", action.getClass().getSimpleName());
                action.execute(bo, conclusion);
                handled = true;
                break;
            }
        }

        if (!handled) {
            log.error("未找到匹配的审核策略 (type={}, status={})", bo.getType(), bo.getStatus());
        }
    }

    /**
     * 2. 取消遍历逻辑
     */
    public void cancel(AftersaleOrder bo) {
        boolean handled = false;

        for (CancelAction action : cancelActions) {
            // Cancel 需要根据 type 和 status 判断
            if (action.supports(bo.getType(), bo.getStatus())) {
                log.debug("匹配到取消策略: {}", action.getClass().getSimpleName());
                action.execute(bo);
                handled = true;
                break;
            }
        }

        if (!handled) {
            //抛异常
            log.warn("未找到匹配的取消策略 (type={}, status={})，将仅修改状态", bo.getType(), bo.getStatus());
        }
    }
}