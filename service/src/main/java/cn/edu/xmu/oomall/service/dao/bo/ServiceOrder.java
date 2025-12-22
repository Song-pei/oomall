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
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static cn.edu.xmu.javaee.core.model.Constants.SYSTEM;

/**
 * 2023-dgn3-009
 *
 * @author huangzian
 * @Slf4j
 * @Builder
 * @NoArgsConstructor
 * @AllArgsConstructor
 * @CopyFrom(ServiceOrderPo.class)
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 * @ToString(exclude = "serviceOrderDao")
 */


@Slf4j
@Builder
@AllArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@CopyFrom(ServiceOrderPo.class)
public class ServiceOrder extends OOMallObject implements Serializable {

    private Long maintainerId;
    private Long shopId;

    private String result;
    private Byte type;//0上门 1寄件 (2线下)
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


    /** 待接受 */
    @JsonIgnore @ToString.Exclude public static final Byte UNACCEPT = 0;
    /** 待派工 */
    @JsonIgnore @ToString.Exclude public static final Byte UNASSIGNED = 1;
    /** 待收件 */
    @JsonIgnore @ToString.Exclude public static final Byte UNCHECK = 2;
    /** 维修中 */
    @JsonIgnore @ToString.Exclude public static final Byte REPAIRING = 3;
    /** 取消 */
    @JsonIgnore @ToString.Exclude public static final Byte CANCEL = 4;
    /** 已完成 */
    @JsonIgnore @ToString.Exclude public static final Byte FINISH = 5;


    /**
     * 状态和名称的对应
     */
    public static final Map<Byte, String> STATUSNAMES = new HashMap() {
        {
            put(UNACCEPT, "待接受");
            put(UNASSIGNED, "待派工");
            put(UNCHECK, "待收件");
            put(REPAIRING, "维修中");
            put(FINISH, "已完成");
            put(CANCEL, "已取消");
        }
    };

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>() {
        {
            put(UNACCEPT, new HashSet<>() {{
                add(UNASSIGNED);
                add(UNCHECK);
                add(CANCEL);
            }});
            put(UNASSIGNED, new HashSet<>() {{
                add(REPAIRING);
                add(CANCEL);
            }});
            put(UNCHECK, new HashSet<>() {{
                add(UNASSIGNED);
                add(CANCEL);
            }});
            put(REPAIRING, new HashSet<>() {{
                add(UNASSIGNED);
                add(FINISH);
                add(CANCEL);
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

        this.status = UNACCEPT;
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
