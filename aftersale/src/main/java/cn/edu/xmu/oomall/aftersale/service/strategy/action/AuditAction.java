package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

/**
 * 审核动作接口
 * 定义所有审核相关的原子操作规范
 */
public interface AuditAction {

    /**
     * 策略路由：判断当前动作是否支持该订单状态和类型
     * @param type 售后类型
     * @param status 当前状态
     * @return true=支持, false=不支持
     */
    boolean supports(Integer type, Integer status);

    /**
     * 执行审核逻辑
     * @param bo 售后单业务对象
     * @param conclusion 审核意见/结论 (这是从 Controller -> Strategy 透传下来的参数)
     */
    // 注意：这里参数要和 TypeStrategy.audit 里的需求对齐
    Integer execute(AftersaleOrder bo, String conclusion);
}