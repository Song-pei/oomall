package cn.edu.xmu.oomall.service.service.strategy.action;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;

/**
 * 取消动作接口 (定义规范)
 */
public interface CancelAction {


    // 1. 定义执行方法
    Byte execute(ServiceOrder serviceOrder, UserToken user);
}