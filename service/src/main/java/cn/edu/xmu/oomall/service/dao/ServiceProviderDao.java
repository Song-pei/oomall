package cn.edu.xmu.oomall.service.dao;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.mapper.ServiceProviderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import cn.edu.xmu.oomall.service.mapper.po.ServiceProviderPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ServiceProviderDao {

    private final ServiceProviderPoMapper serviceProviderPoMapper;

    public void build(ServiceProvider bo) {
        bo.setServiceProviderDao(this);
    }

    public ServiceProvider insert(ServiceProvider bo, UserToken user) {

        bo.setId(null);
        bo.setCreator(user);
        bo.setGmtCreate(LocalDateTime.now());

        ServiceProviderPo po = CloneFactory.copy(new ServiceProviderPo(), bo);
        log.debug("insert: po = {}", po);

        po = this.serviceProviderPoMapper.save(po);

        CloneFactory.copy(bo, po);
        this.build(bo);
        return bo;
    }
    public ServiceProviderPo findPoById(Long id) {
        return serviceProviderPoMapper.findById(id).orElse(null);
    }
    public ServiceProvider findById(Long id) {
        log.debug("findById: id = {}", id);

        ServiceProviderPo po = this.findPoById(id);

        ServiceProvider bo = CloneFactory.copy(new ServiceProvider(), po);
        log.debug("findById: retrieve from database serviceOrder = {}", bo);
        this.build(bo);
        return bo;
    }
}
