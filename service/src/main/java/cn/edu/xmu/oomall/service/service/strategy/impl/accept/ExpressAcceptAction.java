package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.*;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;
import cn.edu.xmu.oomall.service.dao.ServiceProviderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.edu.xmu.javaee.core.model.ReturnObject;

@Slf4j
@Component("expressAcceptAction")
public class ExpressAcceptAction implements AcceptAction {
    @Resource
    private ExpressClient expressClient;
    @Resource
    private ServiceProviderDao serviceProviderDao;
    public Byte execute(ServiceOrder serviceOrder, UserToken user)
    {
        log.info("[FixAuditAction] 开始执行带创建运单的接受服务单逻辑，boId={},", serviceOrder.getId());

        // 1. 组装参数
        ExpressDto dto = new ExpressDto();
        // 使用 dto 实例来创建内部类实例
        dto.setSender(dto.new ContactsInfo());
        dto.setDelivery(dto.new ContactsInfo());
        ServiceProvider serviceProvider = serviceProviderDao.findById(serviceOrder.getMaintainerId());
        //寄件者（服务商）信息
        dto.getSender().setRegionId(serviceProvider.getRegionId());
        dto.getSender().setAddress(serviceProvider.getAddress());
        dto.getSender().setMobile(serviceProvider.getMobile());
        dto.getSender().setName(serviceProvider.getConsignee());
        //收件者信息
        dto.getDelivery().setRegionId(serviceOrder.getRegionId());
        dto.getDelivery().setAddress(serviceOrder.getAddress());
        dto.getDelivery().setMobile(serviceOrder.getMobile());
        dto.getDelivery().setName(serviceOrder.getConsignee());
        //其他信息
        //运单模块没有获取合同的接口，任务无法完成
        dto.setShopLogisticId(Long.valueOf("0"));
        //商品模块没有find相关方法，任务无法完成
        dto.setGoodsType("UNKOWN");

        dto.setWeight(null);
        //无合同相关，无法完成
        dto.setPayMethod(0);
        String token = null;       // 内部调用通常为空，或透传 user token
        Integer userLevel = null;
        try {
            //远程调用
            InternalReturnObject<ExpressPo> ret = expressClient.createPackage(
                    serviceOrder.getShopId(),
                    dto,
                    token,
                    userLevel
            );

            //处理结果
            if (ret.getErrno() == 0 && ret.getData() != null) {
                Long expressId = ret.getData().getId();
                log.info("[ExpressAcceptAction] 接受寄件型服务单并创建运单成功, 运单号: {}", expressId);
                serviceOrder.setExpressId(expressId);
            } else {
                log.error("[ExpressAcceptAction] 物流模块返回错误: {}", ret.getErrmsg());
                throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL, ret.getErrmsg());
            }

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[ExpressAcceptAction] 远程调用异常, boId={}", serviceOrder.getId(), e);
            throw new BusinessException(ReturnNo.REMOTE_SERVICE_FAIL);
        }
        return serviceOrder.UNCHECK;
    }
}
