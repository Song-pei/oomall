package cn.edu.xmu.oomall.payment.domain;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.payment.domain.bo.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public abstract class TransactionDao {

    /**
     * 更新对象
     * @author Ming Qiu
     * <p>
     * date: 2022-11-13 20:46
     * @param trans 更新的值， id不能为null
     * @param userToken
     * @throws RuntimeException
     */
    public <T extends Transaction> void update(T trans, UserToken userToken){

        this.update(trans);
    }

    /**
     * 插入对象
     *
     * @param obj 对象的值
     * @param user 操作用户
     * @throws RuntimeException
     * @author Ming Qiu
     * <p>
     * date: 2022-11-12 20:05
     */
    public <T extends Transaction>  T insert(T obj, UserToken user){
        log.debug("insert: obj = {}", obj);
        obj.setCreator(user);
        obj.setGmtCreate(LocalDateTime.now());
        obj.setId(null);
        Transaction newObj = this.insert(obj);
        return (T) newObj;
    }
    protected abstract Integer update(Transaction obj);

    protected abstract Transaction insert(Transaction obj);

    public abstract Transaction findById(Long shopId, Long id);
}


