package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;

/**
 * 审核动作接口
 * 定义所有审核相关的原子操作规范
 */
public interface AuditAction {


    /**
     * 执行审核逻辑
     * @param bo 售后单业务对象
     * @param conclusion 审核意见/结论 (这是从 Controller -> Strategy 透传下来的参数)
     */
    // 注意：这里参数要和 TypeStrategy.audit 里的需求对齐
    <T> ActionResult<T> execute(AftersaleOrder bo, String conclusion);
}