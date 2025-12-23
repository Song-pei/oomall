package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简单取消策略
 * 场景：刚刚提交申请，还在“待审核”状态，此直接修改状态即可。
 */
@Slf4j
@Component("simpleCancelAction")
public class SimpleCancelAction implements CancelAction {


    @Override
    public Byte execute(ServiceOrder serviceOrder, UserToken user) {
        log.info("[SimpleCancelAction] 命中简单取消策略，boId={}", serviceOrder.getId());
        // TODO: 1. 修改数据库状态为 4 (已取消)
        // TODO: 2. 记录操作日志


        //取消状态变更为 已取消(4)
        return ServiceOrder.CANCEL;
    }
}