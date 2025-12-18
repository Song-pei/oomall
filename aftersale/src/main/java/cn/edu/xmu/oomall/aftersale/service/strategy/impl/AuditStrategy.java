package cn.edu.xmu.oomall.aftersale.service.strategy.impl;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

public interface AuditStrategy {

    // 匹配规则
    boolean match(Integer type, String conclusion);
    // 执行策略
    void execute(AftersaleOrder bo, String conclusion);
}