package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;

public interface InspectAction {

    //定义执行方法
    <T> ActionResult<T> execute(AftersaleOrder bo, UserToken user);
}
