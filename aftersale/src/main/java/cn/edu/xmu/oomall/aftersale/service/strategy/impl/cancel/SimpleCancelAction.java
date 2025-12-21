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
@Component
public class SimpleCancelAction implements CancelAction {

    @Override
    public boolean supports(Integer type, Integer status) {
        // 对应type=0,1,2 且 status=0
        // (只要是待审核状态，无论什么类型，都是简单取消)
        return status == 0;
    }

    @Override
    public Integer execute(AftersaleOrder bo) {
        log.info("[SimpleCancelAction] 命中简单取消策略，boId={}", bo.getId());
        // TODO: 1. 修改数据库状态为 6 (已取消)
        // TODO: 2. 记录操作日志


        //取消状态变更为 已取消(6)
        return AftersaleOrder.CANCEL;
    }
}