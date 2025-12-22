//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.service.service;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceOrderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.strategy.config.StrategyRouter;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
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
        //bo执行业务逻辑
        serviceOrder.accept(user,strategyRouter);
        //更新审计信息
        serviceOrder.setModifier(user);

        serviceOrderDao.save(serviceOrder, user);

        log.info("[Service] 接受完成: boId={}", id);
    }

    public void finish(Long did, long id, String result, UserToken user){
        ServiceOrder serviceOrder =  serviceOrderDao.findById(id);
        serviceOrder.finish(result, user, strategyRouter);
        log.info("[Service] 完成: boId={}", id);
    }

}