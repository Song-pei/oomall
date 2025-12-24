package cn.edu.xmu.oomall.aftersale.service.strategy.action;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.dao.bo.Express;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 取消动作接口 (定义规范)
 */
public interface CancelAction {


    // 1. 定义执行方法
    <T> ActionResult<T> execute(AftersaleOrder bo, Express express,UserToken user);
}