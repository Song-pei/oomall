package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.dao.bo.Express;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;

/**
 * 退货换货取消策略
 * 场景：审核通过进入“待验收”，说明用户可能已经把货寄出了，或者我们需要处理物流信息。
 */
@Slf4j
@Component("expressCancelAction")
public class ExpressCancelAction implements CancelAction {

    @Resource
    private ExpressClient expressClient;
    @Override
    public <T> ActionResult<T> execute(AftersaleOrder bo, Express express , UserToken user) {
        log.info("[ExpressCancelAction] 命中物流拦截策略，boId={}", bo.getId());
        try {


            // 获取 Token
            String token = user.getName();
           /* ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                token = attributes.getRequest().getHeader("authorization");
            }*/
            // 远程调用物流服务
            InternalReturnObject<PackageResponseDTO> ret = expressClient.cancelPackage(
                    bo.getShopId(),
                    express.getExpressId(),
                    token
            );

            //  处理结果
            if (ret.getErrno() == 0 ) {
                PackageResponseDTO packageVo = ret.getData();
                log.info("[ExpressAuditAction] 运单取消成功, ID: {}, 单号: {}", packageVo.getExpressId(),  packageVo.getBillCode());

            } else {
                log.error("[ExpressAuditAction] 物流模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }
            //取消状态变更为 已取消
            return (ActionResult<T>) ActionResult.success(null, AftersaleOrder.CANCEL);
        }  catch (Exception e) {
            log.error("[ExpressCancelAction] 远程调用异常, bo.Id={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "取消运单失败");
        }

    }
}