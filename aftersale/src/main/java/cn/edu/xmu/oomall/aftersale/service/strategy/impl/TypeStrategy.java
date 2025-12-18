package cn.edu.xmu.oomall.aftersale.service.strategy.impl;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

public interface TypeStrategy {
    // 审核方法
    void audit(AftersaleOrder bo, String conclusion);
    // 验收方法
    void accept(AftersaleOrder bo);


}