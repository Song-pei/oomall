//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.payment.domain.bo;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.payment.domain.LedgerDao;
import cn.edu.xmu.oomall.payment.domain.AccountDao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

/**
 * 交易
 */
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
public abstract class Transaction{

    @Getter
    @Setter
    protected Long id;
    @Getter
    @Setter
    protected Instant gmtCreate;
    @Getter
    @Setter
    protected Instant gmtModified;

    /**
     * 内部交易号
     */
    @Getter
    @Setter
    protected String outNo;

    /**
     * 渠道交易号
     */
    @Getter
    @Setter
    protected String transNo;

    /**
     * 金额
     */
    @Getter
    @Setter
    protected Long amount;

    /**
     * 交易时间
     */
    @Getter
    @Setter
    protected Instant successTime;

    /**
     * 状态
     */
    @Getter
    @Setter
    protected Byte status;

    /**
     * 所属商铺id
     */
    @Getter
    @Setter
    protected Long shopId;

    /**
     * 状态迁移
     * @return
     */
    protected abstract Map<Byte, Set<Byte>> getTransition();

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

        if (null != status && null != this.status){
            Set<Byte> allowStatusSet = getTransition().get(this.status);
            if (!Objects.isNull(allowStatusSet)) {
                ret = allowStatusSet.contains(status);
            }
        }
        return ret;
    }

    @Getter
    @Setter
    protected Long accountId;

    /**
     * 交易所属的商铺渠道
     */
    @ToString.Exclude
    @JsonIgnore
    protected Account account;

    @Setter
    @ToString.Exclude
    @JsonIgnore
    protected AccountDao accountDao;

    public Account getAccount() throws BusinessException {
        if (Objects.isNull(this.account) && !Objects.isNull(this.accountDao)) {
            this.account = this.accountDao.findById(PLATFORM, this.accountId);
        }
        return this.account;
    }


    /**
     * 获得支付渠道
     * modified By Rui Li
     * task 2023-dgn1-005
     */
    public Channel getChannel() {
        return this.getAccount().getChannel();
    }
}
