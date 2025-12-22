package cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
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
    public Integer execute(AftersaleOrder bo) {
        log.info("[ExpressCancelAction] 命中物流拦截策略，boId={}", bo.getId());
        try {


            // 4. 远程调用物流服务
            // token 传 null (内部调用通常放行，或使用 RequestContextHolder 获取)
            InternalReturnObject<PackageResponseDTO> ret = expressClient.cancelPackage(
                    bo.getShopId(),
                    bo.getId(),//此处应为运单Id
                    null,   //usertoken
                    0
            );

            // 5. 处理结果
            if (ret.getErrno() == 0 ) {
                PackageResponseDTO packageVo = ret.getData();
                log.info("[ExpressAuditAction] 运单取消成功, ID: {}, 单号: {}", packageVo.getId(), packageVo.getExpressNo());
                // bo.setExpressNo(packageVo.getExpressNo());
            } else {
                log.error("[ExpressAuditAction] 物流模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressCancelAction] 远程调用异常, bo.Id={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "取消运单失败");
        }


        //取消状态变更为 已取消
        return AftersaleOrder.CANCEL;
    }
}