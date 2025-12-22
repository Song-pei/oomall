package cn.edu.xmu.oomall.service.service.strategy.impl.finish;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto; // 请求DTO
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo; // 响应PO
import cn.edu.xmu.oomall.service.service.strategy.action.FinishAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("expressFinishAction")
public class ExpressFinishAction implements FinishAction {

    @Resource
    private ExpressClient expressClient;

    // 如果未来需要查询真实服务商信息，需要注入 ServiceProviderDao
    // @Resource
    // private ServiceProviderDao serviceProviderDao;

    @Override
    public Byte execute(ServiceOrder bo, UserToken user) {
        log.info("[ExpressFinishAction] 开始执行寄件完成策略，boId={}", bo.getId());

        // 1) 组装请求体 (使用 Setter 而非 Builder)
        ExpressDto requestDto = new ExpressDto();

        // ⚠️占位：实际业务中应根据 bo.getShopId() 或 bo.getMaintainerId() 查询商铺绑定的物流渠道ID
        requestDto.setShopLogisticId(0L);

        requestDto.setGoodsType("1"); // 对应DTO中的String类型
        requestDto.setWeight(1L);
        requestDto.setPayMethod(1);   // 1-现付

        // 2) 组装联系人 (解决非静态内部类实例化问题)
        // 必须通过外部类实例 .new 来创建非静态内部类实例
        ExpressDto.ContactsInfo sender = requestDto.new ContactsInfo();

        // 发件人：维修商信息
        sender.setName(bo.getMaintainerName());
        sender.setMobile(bo.getMaintainerMobile());

        // ⚠️占位：以下两项应从 ServiceProvider 表中根据 bo.getMaintainerId() 查询真实数据
        sender.setAddress("服务商默认发货地址");
        sender.setRegionId(0L);

        ExpressDto.ContactsInfo delivery = requestDto.new ContactsInfo();
        // 收件人：客户信息
        delivery.setName(bo.getConsignee());
        delivery.setMobile(bo.getMobile());
        delivery.setAddress(bo.getAddress());
        delivery.setRegionId(bo.getRegionId());

        requestDto.setSender(sender);
        requestDto.setDelivery(delivery);

        // 3) 准备 Header 参数
        String token = null;       // 内部调用通常为空，或透传 user token
        Integer userLevel = null;  // 内部调用

        InternalReturnObject<ExpressPo> ret;
        try {
            // 4) 远程调用 (4参数版本)
            ret = expressClient.createPackage(bo.getShopId(), requestDto, token, userLevel);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressFinishAction] 远程调用异常 boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "物流服务调用失败");
        }

        // 5) 处理结果
        if (ret.getErrno() != 0 || ret.getData() == null) {
            log.error("[ExpressFinishAction] 创建运单失败: {}", ret.getErrmsg());
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "物流服务异常: " + ret.getErrmsg());
        }

        ExpressPo po = ret.getData();
        log.info("[ExpressFinishAction] 运单创建成功: id={}, billCode={}", po.getId(), po.getBillCode());

        // 6) 回填数据到服务单
        bo.setExpressId(po.getId());
        bo.setSerialNo(po.getBillCode());

        return ServiceOrder.FINISH;
    }
}