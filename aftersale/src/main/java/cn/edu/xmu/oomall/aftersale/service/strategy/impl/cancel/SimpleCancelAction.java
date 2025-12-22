package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction; // 确保引用的是你的接口
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
    public Integer execute(AftersaleOrder bo) {
        log.info("[SimpleCancelAction] 命中简单取消策略，boId={}", bo.getId());


        // 4. 记录
        log.info("[SimpleCancelAction] 售后单取消成功, 顾客编号：{},售后单号: {}", bo.getCustomerId(),bo.getId());
        //返回取消状态
        return AftersaleOrder.CANCEL;

    }
}