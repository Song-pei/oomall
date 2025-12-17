package cn.edu.xmu.oomall.service.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceOrderDao;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static cn.edu.xmu.javaee.core.model.Constants.SYSTEM;

/**
 * 2023-dgn3-009
 *
 * @author huangzian
 */
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CopyFrom(ServiceOrderPo.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(exclude = "serviceOrderDao")
public class ServiceOrder extends OOMallObject implements Serializable {

    private Long maintainerId;
    private Long shopId;

    private String result;
    private Byte type;
    private String description;

    private Long regionId;
    private String address;

    private String consignee;
    private String mobile;

    private Byte status;

    private String maintainerMobile;
    private String maintainerName;

    private Long productId;
    private String serialNo;

    @JsonIgnore
    @Setter
    private ServiceOrderDao serviceOrderDao;


    @JsonIgnore
    public static final Byte STATE_WAIT_ACCEPT = 0;
    @JsonIgnore
    public static final Byte STATE_WAIT_DISPATCH = 1;
    @JsonIgnore
    public static final Byte STATE_WAIT_RECEIPT = 2;
    @JsonIgnore
    public static final Byte STATE_UNDER_REPAIR = 3;
    @JsonIgnore
    public static final Byte STATE_COMPLETED = 4;
    @JsonIgnore
    public static final Byte STATE_RETURNED = 5;
    @JsonIgnore
    public static final Byte STATE_CANCELED = 6;

    /**
     * 状态和名称的对应
     */
    public static final Map<Byte, String> STATUSNAMES = new HashMap() {
        {
            put(STATE_WAIT_ACCEPT, "待接受");
            put(STATE_WAIT_DISPATCH, "待派工");
            put(STATE_WAIT_RECEIPT, "待收件");
            put(STATE_UNDER_REPAIR, "维修中");
            put(STATE_COMPLETED, "已完成");
            put(STATE_RETURNED, "已退回");
            put(STATE_CANCELED, "已取消");
        }
    };

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>() {
        {
            put(STATE_WAIT_ACCEPT, new HashSet<>() {{
                add(STATE_WAIT_DISPATCH);
                add(STATE_WAIT_RECEIPT);
                add(STATE_RETURNED);
                add(STATE_CANCELED);
            }});
            put(STATE_WAIT_DISPATCH, new HashSet<>() {{
                add(STATE_UNDER_REPAIR);
                add(STATE_RETURNED);
                add(STATE_CANCELED);
            }});
            put(STATE_WAIT_RECEIPT, new HashSet<>() {{
                add(STATE_WAIT_DISPATCH);
                add(STATE_RETURNED);
                add(STATE_CANCELED);
            }});
            put(STATE_UNDER_REPAIR, new HashSet<>() {{
                add(STATE_COMPLETED);
                add(STATE_CANCELED);
            }});
        }
    };



    /**
     * 是否允许状态迁移
     *
     * @param status
     * @return
     */
    public boolean allowStatus(Byte status) {
        boolean ret = false;
        if (Objects.nonNull(status) && Objects.nonNull(this.status)) {
            Set<Byte> allowStatusSet = toStatus.get(this.status);
            if (Objects.nonNull(allowStatusSet)) {
                ret = allowStatusSet.contains(status);
            }
        }
        return ret;
    }

    /**
     * 获得当前状态名称
     *
     * @return
     */
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }

//    private void changeStatus(Byte status, UserToken user) {
//        log.debug("changeStatus: id = {}, status = {}", this.id, status);
//        if (!this.allowStatus(status)) {
//            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "物流单", this.id, STATUSNAMES.get(this.status)));
//        }
//        this.setStatus(status);
//        this.expressDao.save(this, user);
//    }

    public ServiceOrder create(UserToken user) {

        this.status = STATE_WAIT_ACCEPT;
        log.debug("BO: executing create logic for shopId={}", this.shopId);
        return this.serviceOrderDao.insert(this, user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }

    public Byte getType() { return type; }
    public void setType(Byte type) { this.type = type; }

    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getMaintainerId() { return maintainerId; }
    public void setMaintainerId(Long maintainerId) { this.maintainerId = maintainerId; }

    public String getMaintainerName() { return maintainerName; }
    public void setMaintainerName(String maintainerName) { this.maintainerName = maintainerName; }

    public String getMaintainerMobile() { return maintainerMobile; }
    public void setMaintainerMobile(String maintainerMobile) { this.maintainerMobile = maintainerMobile; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Long getModifierId() {
        return modifierId;
    }

    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

}
