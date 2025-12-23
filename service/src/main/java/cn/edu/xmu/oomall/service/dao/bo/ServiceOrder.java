package cn.edu.xmu.oomall.service.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceOrderDao;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.service.service.strategy.action.FinishAction;
import cn.edu.xmu.oomall.service.service.strategy.config.StrategyRouter;
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
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@CopyFrom(ServiceOrderPo.class)
public class ServiceOrder extends OOMallObject implements Serializable {

    private Long maintainerId;
    private Long shopId;

    private String result;
    private Byte type;//0上门 1寄件 2线上
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
    private Long expressId;
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
            put(CANCEL, "已取消");
            put(FINISH, "已完成");
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

    private void changeStatus(Byte status, UserToken user) {
        log.debug("changeStatus: id = {}, status = {}", this.id, status);
        if (!this.allowStatus(status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "服务单", this.id, STATUSNAMES.get(this.status)));
        }
        this.setStatus(status);
        this.serviceOrderDao.save(this, user);
    }

    public ServiceOrder create(UserToken user) {

        this.status = UNACCEPT;
        log.debug("BO: executing create logic for shopId={}", this.shopId);
        return this.serviceOrderDao.insert(this, user);
    }

    /**
     * 1. 接受服务单
     * @param strategyRouter 传入策略路由工具
     */
    public void accept(UserToken user, StrategyRouter strategyRouter) {
        if (!UNACCEPT.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许接受");
        }
        //使用泛型route方法获取 AuditAction
        AcceptAction action = strategyRouter.route(this.type, this.status, "ACCEPT", AcceptAction.class);

        if (action == null) {
            log.error("未找到接受策略: type={}, status={}", this.type, this.status);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的接受策略");
        }

        // 3. 执行策略并获取目标状态
        Byte nextStatus = action.execute(this, user);

        //结合allowStatus校验状态流转是否合法
        if (!UNASSIGNED.equals(nextStatus)&&!UNCHECK.equals(nextStatus)) {
            log.error("接受通过后试图流转到非法状态: current={}, next={}", this.status, nextStatus);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "接受后状态流转异常");
        }
        this.changeStatus(nextStatus, user);
    }

    /**
     * 2. 取消服务单
     * @param strategyRouter 传入策略路由工具
     */
    public void cancel(UserToken user,StrategyRouter strategyRouter) {
        // 已完成或已取消不允许再次取消
        if (FINISH.equals(this.status) || CANCEL.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消");
        }

        // 通过策略路由找到具体的取消策略
        CancelAction action = strategyRouter.route(this.type, this.status, "CANCEL", CancelAction.class);
        if (action == null) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的取消策略");
        }

        // 执行策略获取目标状态
        Byte nextStatus = action.execute(this,user);

        // 校验状态流转合法性
        if (!CANCEL.equals(nextStatus)) {
            log.error("取消后试图流转到非法状态: current={}, next={}", this.status, nextStatus);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "取消后状态流转异常");
        }
        this.changeStatus(nextStatus, user);
    }



    /**
     * 3. 完成服务单
     * @param strategyRouter 传入策略路由工具
     */
    public void finish(String result, UserToken user, StrategyRouter strategyRouter) {
        // 状态校验：只有维修中才能完成
        if (!REPAIRING.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许完成服务单");
        }

        this.result = result;

        FinishAction action = strategyRouter.route(this.type.byteValue(), this.status.byteValue(), "FINISH", FinishAction.class);
        if (action == null) {
            log.error("未找到完成策略: type={}, status={}", this.type, this.status);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "未配置该类型的完成策略");
        }

        Byte nextStatus = action.execute(this, user);

        if (!FINISH.equals(nextStatus)) {
            log.error("完成策略返回非法目标状态: current={}, next={}", this.status, nextStatus);
            throw new BusinessException(ReturnNo.STATENOTALLOW, "完成后状态流转异常");
        }

        this.changeStatus(nextStatus, user);
    }
    /**
     * 4. 验收包裹
     */
    public void receiveExpress(String result, boolean accepted,UserToken user,StrategyRouter strategyRouter){
        if (!UNCHECK.equals(this.status)) {
            throw new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许完成服务单");
        }
        if(accepted)
            this.changeStatus(UNASSIGNED, user);
        else {
            this.status = UNASSIGNED;
            this.cancel(result, user, strategyRouter);
        }
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
