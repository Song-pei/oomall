package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 维修审核动作
 * 场景：维修单(Type=2) 审核通过时，需要调用第三方服务创建工单
 */
@Slf4j
@Component("simpleAcceptAction")
public class SimpleAcceptAction implements AcceptAction {
    public Byte execute(ServiceOrder serviceOrder, UserToken user)
    {
       return  serviceOrder.UNASSIGNED;
    }
}
