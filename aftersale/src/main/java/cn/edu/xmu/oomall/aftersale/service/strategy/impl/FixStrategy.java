package cn.edu.xmu.oomall.aftersale.service.strategy.impl;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.TypeStrategy;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("2") // 维修策略
public class FixStrategy implements TypeStrategy {

    //  AuditAction 列表 (Spring 会把 FixAuditAction 放进来)
    @Autowired
    private List<AuditAction> auditActions;

    // 注入 CancelAction 列表
    @Autowired
    private List<CancelAction> cancelActions;

    /**
     * 审核逻辑：分发给 Action
     */
    @Override
    public void audit(AftersaleOrder bo, String conclusion) {
        log.info("[FixStrategy] 接收到审核请求，开始寻找合适动作... boId={}", bo.getId());

        // 在列表中找到支持当前状态的 Action
        AuditAction action = auditActions.stream()
                .filter(a -> a.supports(bo.getType(), bo.getStatus()))
                .findFirst()
                // 如果没找到（比如状态不对，或者没写对应的Action），抛出异常
                .orElseThrow(() -> new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不支持审核或策略未定义"));
        // 2. 执行动作 (具体去调远程服务的代码在 FixAuditAction 里)
        action.execute(bo, conclusion);
    }

    /**
     * 取消逻辑：分发给 Action
     */
    @Override
    public void cancel(AftersaleOrder bo) {
        log.info("[FixStrategy] 接收到取消请求... boId={}", bo.getId());

        CancelAction action = cancelActions.stream()
                .filter(a -> a.supports(bo.getType(), bo.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不支持取消操作"));

        action.execute(bo);
    }

    @Override
    public void accept(AftersaleOrder bo) {
        log.info("[FixStrategy] 维修单无需验收");
    }

    @Override
    public void complete(AftersaleOrder bo) {
        log.info("[FixStrategy] 维修单完成逻辑待实现");
    }
}