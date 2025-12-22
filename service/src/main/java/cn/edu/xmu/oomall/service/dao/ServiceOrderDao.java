package cn.edu.xmu.oomall.service.dao;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.mapper.ServiceOrderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.LocalDateTime;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ServiceOrderDao {

    private final ServiceOrderPoMapper serviceOrderPoMapper;

    public void build(ServiceOrder bo) {
        bo.setServiceOrderDao(this);
    }

    public ServiceOrder insert(ServiceOrder bo, UserToken user) {

        bo.setId(null);
        bo.setCreator(user);
        bo.setGmtCreate(LocalDateTime.now());

        ServiceOrderPo po = CloneFactory.copy(new ServiceOrderPo(), bo);
        log.debug("insert: po = {}", po);

        po = this.serviceOrderPoMapper.save(po);

        CloneFactory.copy(bo, po);
        this.build(bo);
        return bo;
    }
    public ServiceOrderPo findById(Long id) {
        return serviceOrderPoMapper.findById(id).orElse(null);
    }
    public void save(ServiceOrderPo po)
    {
        log.debug("DAO执行更新: id={}, po={}", po.getId(), po);
        serviceOrderPoMapper.save(po);
    }
}