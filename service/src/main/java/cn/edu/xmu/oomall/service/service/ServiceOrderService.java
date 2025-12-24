//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.service.service;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceOrderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.strategy.config.StrategyRouter;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderService {
    private final StrategyRouter strategyRouter;
    private final ServiceOrderDao serviceOrderDao;
    
    /*
    * 创建服务单
    * */
    public ServiceOrder createServiceOrder(ServiceOrder serviceOrder, UserToken user) {
        try {

            this.serviceOrderDao.build(serviceOrder);
            log.debug("serviceOrder: serviceOrder = {}", serviceOrder);
            return serviceOrder.create(user);

        } catch (BusinessException e) {
            log.error("创建服务单失败，参数: {}", serviceOrder, e);
            throw new BusinessException(ReturnNo.SERVICE_ORDER_CREATE_FAILED);
        }
    }
    /*
    * 接受服务单
    * auth:Mingyu Li
    * */
    public void acceptServiceOrder(Long did,long id, UserToken user) {
        ServiceOrder serviceOrder =  serviceOrderDao.findById(id);
        serviceOrder.accept(user,strategyRouter);


        log.info("[Service] 接受完成: boId={}", id);
    }

    public void finish(Long did, long id, String result, UserToken user){
        ServiceOrder serviceOrder =  serviceOrderDao.findById(id);

        if (!serviceOrder.getMaintainerId().equals(did) && !serviceOrder.getShopId().equals(did)) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作该服务单");
        }

        serviceOrder.finish(result, user, strategyRouter);
        log.info("[Service] 完成: boId={}", id);
    }
    /*
    * 服务商验收包裹
    * */
    public void receiveExpress(Long did,long id,String result, boolean accepted,UserToken user){
        ServiceOrder serviceOrder =  serviceOrderDao.findById(id);
        serviceOrder.receiveExpress(result,accepted,user,strategyRouter);
        log.info("[Service] 完成: boId={}", id);
    }
    /*
     * 取消服务单
     * */
    public void cancelServiceOrder(Long id, UserToken user) {
        ServiceOrder serviceOrder = serviceOrderDao.findById(id);
        serviceOrder.cancel(user,strategyRouter);

        log.info("[Service] 取消完成: boId={}", id);
    }

    /**
     * 根据 ID 查询服务单
     * * @param shopId 商铺 ID（用于权限校验，确保当前商铺只能访问自己的单据）
     * @param id 服务单 ID
     * @return 完整填充的领域对象 ServiceOrder
     * @throws BusinessException 
     * 1. 资源不存在 (RESOURCE_ID_NOTEXIST, 对应 HTTP 404)
     * 2. 权限不足 (RESOURCE_ID_OUTSCOPE, 对应 HTTP 403)
     */
    @Transactional(readOnly = true)
    public ServiceOrder findById(Long shopId, Long id) {
        log.debug("findById: shopId = {}, id = {}", shopId, id);

        // 1. 获取领域对象 (BO)
        ServiceOrder serviceOrder = this.serviceOrderDao.findById(id);

        // 2. 严格的存在性校验
        if (Objects.isNull(serviceOrder)) {
            log.error("findById: 服务单不存在, id = {}", id);
            // 仿照 AdminRegionController 使用标准错误码和 String.format 填充消息
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, 
                    String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "服务单", id));
        }

        // 3. 安全性校验：防止水平越权
        // 只有当明确传入 shopId 时才校验，这允许管理员接口（shopId 为空）复用此方法
        if (Objects.nonNull(shopId) && !Objects.equals(serviceOrder.getShopId(), shopId)) {
            log.warn("findById: 越权访问尝试! serviceOrderId={}, requestedShopId={}, actualShopId={}", 
                    id, shopId, serviceOrder.getShopId());
            // 抛出 403 异常，消息内容对齐项目规范
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE,
                    String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "服务单", id, shopId));
        }

        return serviceOrder;
    }
        /*
     * 维修师父退回服务单
     * */
    public void backServiceOrder(Long did, long id, String result, UserToken user) {
        ServiceOrder serviceOrder = serviceOrderDao.findById(id);
        serviceOrder.setResult(result);
        serviceOrder.backServiceOrder(user);
        log.info("[Service] 维修师傅退回待派工完成: boId={}", id);
            }
}