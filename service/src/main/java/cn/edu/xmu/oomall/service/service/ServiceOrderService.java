//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.service.service;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceOrderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
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

    private final ServiceOrderDao serviceOrderDao;

    public ServiceOrder createServiceOrder(ServiceOrder serviceOrder, UserToken user) {
        try {

            this.serviceOrderDao.build(serviceOrder);
            log.debug("serviceOrder: serviceOrder = {}", serviceOrder);
            return serviceOrder.create(user);

        } catch (BusinessException e) {
            throw new BusinessException(ReturnNo.INTERNAL_SERVER_ERR, "创建服务单失败");
        }
    }
}