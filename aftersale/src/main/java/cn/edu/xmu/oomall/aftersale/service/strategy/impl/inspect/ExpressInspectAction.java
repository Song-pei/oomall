package cn.edu.xmu.oomall.aftersale.service.strategy.impl.inspect;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.InspectAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("expressInspectAction")
public class ExpressInspectAction implements InspectAction {
    @Resource
    private ExpressClient expressClient;
    @Override
    public Integer execute(AftersaleOrder bo, UserToken user) {
        log.info("[ExpressInspectAction] 开始执行退换货验收策略 aftersaleId={}", bo.getId());
        try{
            // 1. 构建 DTO
            PackageCreateDTO.Contact sender = PackageCreateDTO.Contact.builder()
                    .name("商家货仓")
                    .mobile("13800000000")
                    .regionId(188L)
                    .address("福建省厦门市思明区商家默认仓库")
                    .build();

            PackageCreateDTO.Contact delivery = PackageCreateDTO.Contact.builder()
                    .name(bo.getCustomerName())
                    .mobile(bo.getCustomerMobile())
                    .regionId(bo.getCustomerRegionId())
                    .address(bo.getCustomerAddress())
                    .build();

            PackageCreateDTO createDto = PackageCreateDTO.builder()
                    .sender(sender)
                    .delivery(delivery)
                    .shopLogisticId(1L)
                    .goodsType("换货商品")
                    .weight(1L)
                    .payMethod(2)
                    .build();
            String token=user.getName();
            // 2. 远程调用
            InternalReturnObject<PackageResponseDTO> ret = expressClient.createPackage(
                    bo.getShopId(),
                    createDto,
                    token
            );
            if (ret.getErrno() == 0 && ret.getData() != null) {
                PackageResponseDTO packageVo = ret.getData();

                bo.setExchangeExpressId(ret.getData().getId());// 设置换货运单ID

                log.info("[ExpressAuditAction] 换货运单创建成功, ID: {}, 单号: {}", packageVo.getId(), packageVo.getBillCode());
            } else {
                log.error("[ExpressAuditAction] 物流模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressInspectAction] 远程调用异常, boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "创建换货运单失败");
        }
        return AftersaleOrder.UNCHANGE;
    }
}