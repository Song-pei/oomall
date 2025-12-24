package cn.edu.xmu.oomall.service.service.strategy.action;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;

/*
* 接受服务单动作
* auth:Mingyu Li
* */
public interface AcceptAction {
    Byte execute(ServiceOrder serviceOrder, ServiceProvider serviceProvider, UserToken user);
}
