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
    public Integer audit(AftersaleOrder bo, String conclusion) {
        boolean handled = false;

        for (AuditAction action : auditActions) {
            // 利用 action 自己的 supports 方法判断是否匹配
            if (action.supports(bo.getType(), bo.getStatus())) {
                log.debug("匹配到审核策略: {}", action.getClass().getSimpleName());

                // 直接执行并返回 Action 给出的状态码
                return action.execute(bo, conclusion);
            }
        }
        // 没找到策略
        log.error("未找到匹配的审核策略 (type={}, status={})", bo.getType(), bo.getStatus());
        return null;

    }

    /**
     * 2. 取消遍历逻辑
     */
    public Integer cancel(AftersaleOrder bo) {
        for (CancelAction action : cancelActions) {
            // Cancel 需要根据 type 和 status 判断
            if (action.supports(bo.getType(), bo.getStatus())) {
                log.debug("匹配到取消策略: {}", action.getClass().getSimpleName());
                //接执行并返回 Action 给出的状态码
                return action.execute(bo);
            }
        }

        log.warn("未找到匹配的取消策略 (type={}, status={})，将仅修改状态", bo.getType(), bo.getStatus());
        return null;
    }
}