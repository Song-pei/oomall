package cn.edu.xmu.oomall.service.service.strategy.impl.finish;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.service.strategy.action.FinishAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("expressFinishAction")
public class ExpressFinishAction implements FinishAction {

    @Resource
    private ExpressClient expressClient;

    @Override
    public Byte execute(ServiceOrder bo, ServiceProvider serviceProvider,UserToken user) {
        log.info("[ExpressFinishAction] 开始执行寄件完成策略，boId={}", bo.getId());

        // 1. 组装请求体
        ExpressDto requestDto = new ExpressDto();
        requestDto.setShopLogisticId(0L);
        requestDto.setGoodsType("1");
        requestDto.setWeight(1L);
        requestDto.setPayMethod(1);

        // 2. 组装联系人
        ExpressDto.ContactsInfo sender = requestDto.new ContactsInfo();

        sender.setRegionId(serviceProvider.getRegionId());
        sender.setAddress(serviceProvider.getAddress());
        sender.setMobile(serviceProvider.getMobile());
        sender.setName(serviceProvider.getConsignee());

        ExpressDto.ContactsInfo delivery = requestDto.new ContactsInfo();
        delivery.setName(bo.getConsignee());
        delivery.setMobile(bo.getMobile());
        delivery.setAddress(bo.getAddress());
        delivery.setRegionId(bo.getRegionId());

        requestDto.setSender(sender);
        requestDto.setDelivery(delivery);

        // 3. 安全获取 Header 参数 (防止 NPE)
        // 注意：根据你的报错日志，UserToken 的方法名是 getUserLevel()
        String userName = Optional.ofNullable(user)
                .map(UserToken::getName)
                .orElse("system");

        Integer userLevel = Optional.ofNullable(user)
                .map(UserToken::getUserLevel)
                .orElse(1);

        InternalReturnObject<ExpressPo> ret;
        try {
            // 4. 远程调用
            ret = expressClient.createPackage(bo.getShopId(), requestDto, userName, userLevel);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressFinishAction] 远程调用异常 boId={}", bo.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "物流服务调用失败");
        }

        // 5. 防御性检查 ret 是否为 null
        // 如果 Mockito 打桩没匹配上，ret 会是 null，这里必须拦截，否则下面 getErrno 会报 NPE
        if (ret == null) {
            log.error("[ExpressFinishAction] 远程调用返回 null (可能是 Mock 参数不匹配)");
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "物流服务无响应");
        }

        // 6. 处理业务结果
        if (ret.getErrno() != 0 || ret.getData() == null) {
            log.error("[ExpressFinishAction] 创建运单失败: {}", ret.getErrmsg());
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, "物流服务异常: " + ret.getErrmsg());
        }

        ExpressPo po = ret.getData();
        log.info("[ExpressFinishAction] 运单创建成功: id={}, billCode={}", po.getId(), po.getBillCode());

        bo.setExpressId(po.getId());
        bo.setSerialNo(po.getBillCode());

        return ServiceOrder.FINISH;
    }
}