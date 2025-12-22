package cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退换货审核动作
 * 适用场景：退货(0) 或 换货(1) 的待审核(0) 状态
 * 动作：审核通过 -> 调用物流服务创建运单(供用户寄回) -> 状态变更为 待验收(1)
 */
@Slf4j
@Component("expressAuditAction")
public class ExpressAuditAction implements AuditAction {

    @Resource
    private ExpressClient expressClient;

    @Override
    public Integer execute(AftersaleOrder bo, String conclusion) {
        log.info("[ExpressAuditAction] 命中退换货审核策略，开始执行... boId={}, type={}", bo.getId(), bo.getType());

        try {
            // 1. 构建寄件人信息 (Customer - 从售后单 BO 获取)
            // 这里的业务逻辑是：生成一张运单，让顾客把货退回来
            PackageCreateDTO.Contact sender = PackageCreateDTO.Contact.builder()
                    .name(bo.getCustomerName())
                    .mobile(bo.getCustomerMobile())
                    .regionId(bo.getCustomerRegionId())
                    .address(bo.getCustomerAddress())
                    .build();

            // 2. 构建收件人信息 (Shop - 商家仓库)
            // 先暂时模拟一个默认地址
            PackageCreateDTO.Contact delivery = PackageCreateDTO.Contact.builder()
                    .name("商家退货仓")
                    .mobile("13800000000")
                    .regionId(188L) // 假设的厦门地区ID
                    .address("福建省厦门市思明区商家默认仓库")
                    .build();

            // 3. 组装完整的创建请求 DTO
            PackageCreateDTO createDto = PackageCreateDTO.builder()
                    .sender(sender)
                    .delivery(delivery)
                    .shopLogisticId(null)
                    .goodsType("退货商品")
                    .weight(1)
                    .payMethod(2)
                    .build();

            // 4. 远程调用物流服务
            // token 传 null (内部调用通常放行，或使用 RequestContextHolder 获取)
            InternalReturnObject<PackageResponseDTO> ret = expressClient.createPackage(
                    bo.getShopId(),
                    createDto,
                    null
            );

            // 5. 处理结果
            if (ret.getErrno() == 0 && ret.getData() != null) {
                PackageResponseDTO packageVo = ret.getData();
                log.info("[ExpressAuditAction] 运单创建成功, ID: {}, 单号: {}", packageVo.getId(), packageVo.getExpressNo());
                // bo.setExpressNo(packageVo.getExpressNo());
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

        // 6. 返回新的状态：待验收(1)
        return AftersaleOrder.UNCHECK;
    }
}