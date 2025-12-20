package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

/**
 * 取消动作接口 (定义规范)
 */
public interface CancelAction {

    // 1. 定义筛选方法
    boolean supports(Integer type, Integer status);

    // 2. 定义执行方法
    void execute(AftersaleOrder bo);
}