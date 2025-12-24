package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简单取消策略
 * 场景：刚刚提交申请，还在“待审核”状态，此直接修改状态即可。
 */
@Slf4j
@Component("simpleCancelAction")
public class SimpleCancelAction implements CancelAction {
    public Byte execute(ServiceOrder serviceOrder, ServiceProvider serviceProvider,UserToken user) {return ServiceOrder.CANCEL;}
}