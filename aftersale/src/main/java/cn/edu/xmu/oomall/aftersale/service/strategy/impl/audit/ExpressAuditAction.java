package cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component("expressAuditAction")
@SuppressWarnings("unchecked")
public class ExpressAuditAction implements AuditAction {

    @Resource
    private ExpressClient expressClient;

    @Override
    public <T> ActionResult<T> execute(AftersaleOrder bo, String conclusion) {
        log.info("[ExpressAuditAction] 命中退换货审核策略，开始执行... boId={}, type={}", bo.getId(), bo.getType());

        try {
            // 1. 构建 DTO
            PackageCreateDTO.Contact sender = PackageCreateDTO.Contact.builder()
                    .name(bo.getCustomerName())
                    .mobile(bo.getCustomerMobile())
                    .regionId(bo.getCustomerRegionId())
                    .address(bo.getCustomerAddress())
                    .build();

            PackageCreateDTO.Contact delivery = PackageCreateDTO.Contact.builder()
                    .name("商家退货仓")
                    .mobile("13800000000")
                    .regionId(188L)
                    .address("福建省厦门市思明区商家默认仓库")
                    .build();

            PackageCreateDTO createDto = PackageCreateDTO.builder()
                    .sender(sender)
                    .delivery(delivery)
                    .shopLogisticId(1L)
                    .goodsType("退货商品")
                    .weight(1L)
                    .payMethod(2)
                    .build();

            // 2. 获取 Token
            String token = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                token = attributes.getRequest().getHeader("authorization");
            }

            // 3. 远程调用
            InternalReturnObject<PackageResponseDTO> ret = expressClient.createPackage(
                    bo.getShopId(),
                    createDto,
                    token
            );

            // 4. 处理结果
            if (ret.getErrno() == 0 && ret.getData() != null) {
                PackageResponseDTO packageVo = ret.getData();

                bo.setCustomerExpressId(packageVo.getId());
                log.info("[ExpressAuditAction] 运单创建成功, ID: {}, 单号: {}", packageVo.getId(), packageVo.getBillCode());


            } else {
                log.error("[ExpressAuditAction] 物流模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressAuditAction] 远程调用异常, boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "创建退货运单失败");
        }

        return (ActionResult<T>) ActionResult.success(null, AftersaleOrder.UNCHECK);

    }
}