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
        serviceOrder.finish(result, user, strategyRouter);
        log.info("[Service] 完成: boId={}", id);
    }
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