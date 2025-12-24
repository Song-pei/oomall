package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.dao.bo.Express;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction; // 确保引用的是你的接口
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
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
    public <T> ActionResult<T> execute(AftersaleOrder bo,  UserToken user) {
        log.info("[SimpleCancelAction] 命中简单取消策略，boId={}", bo.getId());

        log.info("[SimpleCancelAction] 售后单取消成功, 顾客编号：{},售后单号: {}", bo.getCustomerId(),bo.getId());
        //返回取消状态
        return (ActionResult<T>) ActionResult.success(null, AftersaleOrder.CANCEL);
    }
}