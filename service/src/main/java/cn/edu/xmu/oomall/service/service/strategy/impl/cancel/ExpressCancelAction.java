package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退货换货取消策略
 * 场景：审核通过进入“待验收”，说明用户可能已经把货寄出了，或者我们需要处理物流信息。
 */
@Slf4j
@Component("expressCancelAction")
public class ExpressCancelAction implements CancelAction {


    @Override
    public Byte execute(ServiceOrder bo) {
        log.info("[ExpressCancelAction] 命中物流拦截策略，boId={}", bo.getId());
        // TODO:

        //取消状态变更为 已取消
        return ServiceOrder.CANCEL;
    }
}