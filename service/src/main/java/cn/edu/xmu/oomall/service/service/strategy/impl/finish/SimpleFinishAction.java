package cn.edu.xmu.oomall.service.service.strategy.impl.finish;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.strategy.action.FinishAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 上门维修完成动作
 * 场景：上门服务单(Type=0) 在维修中(REPAIRING=3)  返回已完成(FINISH=5)
 */
@Slf4j
@Component("simpleFinishAction")
public class SimpleFinishAction implements FinishAction {

    @Override
    public Byte execute(ServiceOrder serviceOrder, ServiceProvider serviceProvider,UserToken user) {

        log.debug("SimpleFinishAction: serviceOrderId={}, type={}, status={}",
                serviceOrder.getId(), serviceOrder.getType(), serviceOrder.getStatus());

        return serviceOrder.FINISH;
    }
}
