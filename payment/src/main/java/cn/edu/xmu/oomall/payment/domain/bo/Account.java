//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.payment.domain.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.payment.domain.ChannelDao;
import cn.edu.xmu.oomall.payment.domain.PayTransDao;
import cn.edu.xmu.oomall.payment.domain.AccountDao;
import cn.edu.xmu.oomall.payment.domain.channel.PayAdaptor;
import cn.edu.xmu.oomall.payment.domain.channel.PayAdaptorFactory;
import cn.edu.xmu.oomall.payment.domain.channel.vo.PostPayTransAdaptorVo;
import cn.edu.xmu.oomall.payment.infrastructure.mapper.po.AccountPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import static cn.edu.xmu.oomall.payment.domain.bo.PayTrans.NEW;

/**
 * 商铺收款账号
 */
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyFrom({AccountPo.class})
@CopyTo(AccountPo.class)
@Slf4j
public class Account implements Serializable {

    /**
     * 有效
     */
    @ToString.Exclude
    @JsonIgnore
    public static Byte VALID = 0;
    /**
     * 无效
     */
    @ToString.Exclude
    @JsonIgnore
    public static Byte INVALID = 1;

    /**
     *  删除
     */
    @ToString.Exclude
    @JsonIgnore
    public static Byte DELETE = 2;

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>(){
        {
            put(VALID, new HashSet<>(){
                {
                    add(INVALID);
                }
            });
            put(INVALID, new HashSet<>(){
                {
                    add(VALID);
                    add(DELETE);
                }
            });
        }
    };

    /**
     * 状态和名称的对应
     */
    public static final Map<Byte, String> STATUSNAMES = new HashMap(){
        {
            put(VALID, "有效");
            put(INVALID, "无效");
            put(DELETE, "删除");
        }
    };
    /**
     * 是否允许状态迁移
     * @author Ming Qiu
     * <p>
     * date: 2022-11-13 0:25
     * @param status
     * @return
     */
    public boolean allowStatus(Byte status){
        boolean ret = false;

        if (!Objects.isNull(status) && !Objects.isNull(this.status)){
            Set<Byte> allowStatusSet = toStatus.get(this.status);
            if (!Objects.isNull(allowStatusSet)) {
                ret = allowStatusSet.contains(status);
            }
        }
        return ret;
    }

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String creator;
    @Setter
    @Getter
    private String modifier;
    @Setter
    @Getter
    private Instant gmtCreate;
    @Setter
    @Getter
    private Instant gmtModified;
    /**
     * 商铺id
     */
    @Setter
    @Getter
    private Long shopId;

    /**
     * 子商户号
     */
    @Setter
    @Getter
    private String subMchid;

    /**
     * 状态
     */
    @Setter
    private Byte status;

    /**
     * 支付渠道
     */
    @ToString.Exclude
    @JsonIgnore
    private Channel channel;

    @Setter
    @Getter
    private Long channelId;

    @ToString.Exclude
    @JsonIgnore
    @Setter
    private ChannelDao channelDao;

    @ToString.Exclude
    @JsonIgnore
    @Setter
    private AccountDao accountDao;

    @ToString.Exclude
    @JsonIgnore
    private PayAdaptor payAdaptor;

    public void setPayAdaptor(PayAdaptorFactory factory){
        this.payAdaptor = factory.createPayAdaptor(this.getChannel());
    }

    public Channel getChannel(){
        if (Objects.isNull(this.channel) && Objects.nonNull(this.channelDao)){
            log.debug("getChannel: this.channelId = {}", this.channelId);
            this.channel = this.channelDao.findById(this.channelId);
        }
        return this.channel;
    }

    /**
     * 解约账户
     */
    public String cancel(UserToken user){
        //如果该商铺支付渠道的状态是有效的，不允许删除
        String key =  this.changeStatus(DELETE, user);
        this.payAdaptor.cancelChannel(this);
        return key;
    }
    @JsonIgnore
    public Byte getStatus() {
        Channel channel = this.getChannel();
        if (Channel.VALID.equals(channel.getStatus())) {
            return this.status;
        }else{
            //支付渠道无效
            return Account.INVALID;
        }
    }

    /**
     * 有效商户支付渠道
     * @param user
     */
    public String valid(UserToken user){
        return this.changeStatus(VALID, user);
    }

    /**
     * 无效商户支付渠道
     * @param user
     */
    public String invalid(UserToken user){
        return this.changeStatus(INVALID, user);
    }

    /**
     * 修改状态
     * @param status 状态
     * @param user 操作者
     * @return
     */
    private String changeStatus(Byte status, UserToken user){
        if (!this.allowStatus(status)){
            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "商铺", this.id, STATUSNAMES.get(this.status)));
        }
        Account newAccount = new Account();
        newAccount.setStatus(status);
        newAccount.setId(id);
        newAccount.setChannelId(this.channelId);
        return this.accountDao.update(newAccount, user);
    }
    @ToString.Exclude
    @JsonIgnore
    @Setter
    private PayTransDao payTransDao;

    /**
     * 创建支付
     * @param payTrans 支付交易值对象
     * @param user 操作者
     * @return 支付渠道返回值
     * modified by ych
     * task-2023-dgn1-004
     */
    public PostPayTransAdaptorVo createPayment(PayTrans payTrans, UserToken user){
        if (this.status.equals(INVALID)) {
            throw new BusinessException(ReturnNo.PAY_CHANNEL_INVALID, String.format(ReturnNo.PAY_CHANNEL_INVALID.getMessage(), this.getChannel().getName()));
        }
        payTrans.setShopId(this.shopId);
        payTrans.setStatus(NEW);
        PayTrans newObj = this.payTransDao.insert(payTrans, user);
        log.debug("createPayment: payAdaptor = {}, newObj = {}", payAdaptor, newObj);

        PostPayTransAdaptorVo adaptorVo = this.payAdaptor.createPayment(newObj);
        PayTrans updateTrans = new PayTrans();
        updateTrans.setId(newObj.getId());
        if (Objects.nonNull(adaptorVo.getPrepayId())) {
            updateTrans.setPrepayId(adaptorVo.getPrepayId());
        }
        adaptorVo.setId(newObj.getId());
        this.payTransDao.update(updateTrans, user);

        log.debug("createPayment: dto = {}", adaptorVo);
        return adaptorVo;
    }
}
