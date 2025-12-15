//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.payment.domain.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.payment.domain.PayTransDao;
import cn.edu.xmu.oomall.payment.domain.RefundTransDao;
import cn.edu.xmu.oomall.payment.domain.channel.PayAdaptor;
import cn.edu.xmu.oomall.payment.domain.channel.PayAdaptorFactory;
import cn.edu.xmu.oomall.payment.domain.channel.vo.PostRefundAdaptorVo;
import cn.edu.xmu.oomall.payment.infrastructure.mapper.po.PayTransPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

import static cn.edu.xmu.javaee.core.model.Constants.MAX_RETURN;
import static cn.edu.xmu.javaee.core.model.Constants.SYSTEM;

/**
 * 支付交易
 */
@ToString(callSuper = true, doNotUseGetters = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@CopyFrom({PayTransPo.class})
@CopyTo({PayTransPo.class})
@Slf4j
public class PayTrans extends Transaction{

    /**
     * 未支付
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte NEW = 0;
    /**
     * 已支付
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte SUCCESS = 1;
    /**
     * 错账
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte WRONG = 3;
    /**
     * 支付失败
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte FAIL = 4;
    /**
     * 取消
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte CANCEL = 5;
    /**
     * 分账
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte DIV = 7;

    /**
     * 状态和名称的对应
     */
    public static final Map<Byte, String> STATUSNAMES = new HashMap(){
        {
            put(NEW, "待支付");
            put(SUCCESS, "已支付");
            put(WRONG, "错账");
            put(FAIL, "支付失败");
            put(CANCEL, "取消");
            put(DIV, "分账");
        }
    };

    protected Map<Byte, Set<Byte>> getTransition(){
        return toStatus;
    };

    /**
     * 允许的状态迁移
     */
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>(){
        {
            put(NEW, new HashSet<>(){
                {
                    add(FAIL);
                    add(CANCEL);
                    add(SUCCESS);
                    add(WRONG);
                }
            });
            put(SUCCESS, new HashSet<>(){
                {
                    add(DIV);
                }
            });
            put(WRONG, new HashSet<>(){
                {
                    add(SUCCESS);
                }
            });
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

        if (Objects.nonNull(status) && Objects.nonNull(this.status)){
            Set<Byte> allowStatusSet = toStatus.get(this.status);
            if (Objects.nonNull(allowStatusSet)) {
                ret = allowStatusSet.contains(status);
            }
        }
        return ret;
    }

    @ToString.Exclude
    @JsonIgnore
    private PayAdaptor payAdaptor;

    /**
     * modified by ych
     * task 2023-dgn1-004
     */
    public void setPayAdaptor(PayAdaptorFactory factory){
        this.payAdaptor = factory.createPayAdaptor(this.getChannel());
    }


    @ToString.Exclude
    @JsonIgnore
    @Setter
    private PayTransDao payTransDao;


    /**
     * 获得当前状态名称
     * @author Ming Qiu
     * <p>
     * date: 2022-11-13 0:43
     * @return
     */
    public String getStatusName(){
        return STATUSNAMES.get(this.status);
    }

    /**
     * 退款
     * @param amount 退款金额
     * @param divAmount 退款分账金额
     * @param user 操作者
     * @return
     */
    public RefundTrans refund(Long amount, Long divAmount, UserToken user){

        //先改变支付交易在退款中，不能允许支付交易同时处理两笔退款, 写操作会锁住其他操作
        PayTrans temp = new PayTrans();
        temp.setId(this.id);
        temp.setInRefund(PayTrans.REFUNDING);
        this.payTransDao.update(temp, user);

        //创建退款交易
        RefundTrans trans = new RefundTrans(this, amount, divAmount);
        RefundTrans newTrans = this.refundTransDao.insert(trans, user);
        //获得完整的bo对象
        RefundTrans refundTrans = this.refundTransDao.findById(trans.getShopId(), newTrans.getId());

        try {
            //查询是否分账
            if (this.status.equals(PayTrans.DIV)) {
                DivPayTrans divPayTrans = this.getDivTrans();
                divPayTrans.refund(refundTrans, user);
            }
            PostRefundAdaptorVo vo = this.payAdaptor.createRefund(refundTrans);
            refundTrans.setTransNo(vo.getTransNo());
            refundTrans.setAmount(vo.getAmount());
            refundTrans.setId(newTrans.getId());
            refundTrans.setSuccessTime(LocalDateTime.now());
            this.refundTransDao.update(refundTrans, user);
        }catch (Exception e){
            refundTrans.setStatus(RefundTrans.FAIL);
            this.refundTransDao.update(refundTrans, user);
            throw e;
        }
        return refundTrans;
    }

    /**
     * modified ych
     * task 2023-dgn1-004
     * 取消支付
     * @param user
     */
    public void cancel(UserToken user){
        if (!this.allowStatus(CANCEL)){
            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "支付交易",id, this.getStatusName()));
        }
        this.payAdaptor.cancelOrder(this);
        PayTrans newTrans = new PayTrans();
        newTrans.setStatus(CANCEL);
        newTrans.setId(this.getId());
        this.payTransDao.update(newTrans,user);
    }

    /**
     * 支付单描述
     */
    @Setter
    @Getter
    private String description;

    /**
     * 支付用户id
     */
    @Setter
    @Getter
    private String spOpenid;

    /**
     * 交易结束时间
     */
    @Setter
    @Getter
    private LocalDateTime timeExpire;

    /**
     * 交易开始时间
     */
    @Setter
    @Getter
    private LocalDateTime timeBegin;

    /**
     * 预支付id
     */
    @Setter
    @Getter
    private String prepayId;

    /**
     * 待分账金额
     */
    @Setter
    @Getter
    private Long divAmount;

    /**
     * 退款中
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte REFUNDING = 1;

    /**
     * 0 正常 1退款中
     */
    @Setter
    @Getter
    private Byte inRefund;

    @ToString.Exclude
    @JsonIgnore
    @Setter
    private RefundTransDao refundTransDao;
    /**
     * 关联的退款交易
     */
    @ToString.Exclude
    @JsonIgnore
    private List<RefundTrans> refundTransList;

    public List<RefundTrans> getRefundTransList() throws BusinessException {
        if (Objects.isNull(this.refundTransList) && Objects.nonNull(this.refundTransDao)){
            this.refundTransList = ;
        }
        return this.refundTransList;
    }

    /**
     * 已经退回和正在处理中的退款总额
     * @author Ming Qiu
     * <p>
     * date: 2022-11-15 15:25
     * @return
     * modified By Rui Li
     * task 2023-dgn1-005
     */
    public Long getRefundAmount(){
        return this.getRefundTransList().stream()
            .filter(trans -> RefundTrans.FAIL != trans.getStatus())
            .map(RefundTrans::getAmount)
            .reduce((x,y)->x + y)
            .orElse(0L);
    }

    /**
     * 创建支付交易
     * @author Ming Qiu
     * <p>
     * date: 2022-11-14 6:43
     * @param account
     * @param spOpenid
     * @param timeExpire 过期时间，单位秒
     * @param amount
     */
    public PayTrans(LocalDateTime timeBegin, LocalDateTime timeExpire,  String spOpenid,  Long amount, long divAmount, Account account) {
        this.spOpenid = spOpenid;
        this.timeExpire = timeExpire;
        this.spOpenid = spOpenid;
        this.accountId = account.getId();
        this.amount = amount;
        this.timeBegin = timeBegin;
        this.timeExpire = timeExpire;
        this.divAmount = divAmount;
    }
}
