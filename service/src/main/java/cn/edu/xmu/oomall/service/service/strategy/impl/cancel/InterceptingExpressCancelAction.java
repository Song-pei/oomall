package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 物流拦截策略
 * 场景：在“待收件”状态中，此时取消服务单，需要对寄来的物品进行物流拦截
 */
@Slf4j
@Component("interceptingExpressCancelAction")
public class InterceptingExpressCancelAction implements CancelAction {
    @Resource
    private ExpressClient expressClient;
    public Byte execute(ServiceOrder serviceOrder, UserToken user){
        log.info("[InterceptingCancelAction] 命中物流拦截策略，serviceOrderId={}", serviceOrder.getId());
        String token = null;

        try {
            // 2. 远程调用
            InternalReturnObject<ExpressPo> ret = expressClient.cancelPackage(
                    serviceOrder.getShopId(),
                    serviceOrder.getExpressId(),
                    user,
                    user.getUserLevel()
            );

            // 3. 处理结果
            if (ret.getErrno() == 0 && ret.getData() != null) {
                Long expressId = ret.getData().getId();
                log.info("[InterceptingCancelAction] 物流拦截成功, 运单号: {}", expressId);
            } else {
                log.error("[InterceptingCancelAction] 服务模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        }  catch (Exception e) {
            log.error("[InterceptingCancelAction] 远程调用异常, boId={}", serviceOrder.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }

        //取消状态变更为 已取消
        return ServiceOrder.CANCEL;
    }
}
