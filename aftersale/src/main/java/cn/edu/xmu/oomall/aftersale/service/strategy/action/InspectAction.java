package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

public interface InspectAction {

    //定义执行方法
    Integer execute(AftersaleOrder bo, UserToken user);
}
