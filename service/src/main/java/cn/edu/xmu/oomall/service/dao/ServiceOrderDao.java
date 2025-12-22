package cn.edu.xmu.oomall.service.dao;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.mapper.ServiceOrderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Objects;
import java.time.LocalDateTime;

import static cn.edu.xmu.javaee.core.model.Constants.IDNOTEXIST;

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

    public ServiceOrder findById(@NotNull Long id) {
        log.debug("findById: id = {}", id);

        ServiceOrderPo po = this.findPoById(id);

        ServiceOrder bo = CloneFactory.copy(new ServiceOrder(), po);
        log.debug("findById: retrieve from database serviceOrder = {}", bo);

        this.build(bo);
        return bo;
    }

    public void save(@NotNull ServiceOrder bo, UserToken user){
        ServiceOrderPo oldPo = this.findPoById(bo.getId());
        bo.setModifier(user);
        bo.setGmtModified(LocalDateTime.now());
        ServiceOrderPo po = CloneFactory.copyNotNull(oldPo, bo);
        log.debug("save: po = {}", po);
        this.serviceOrderPoMapper.save(po);
    }

    /**
     * 按照id找到Po对象
     * @param id 对象id
     * @return po对象
     */
    private ServiceOrderPo findPoById(@NotNull Long id){
        return this.serviceOrderPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST,
                        String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "服务单", id)));
    }
}