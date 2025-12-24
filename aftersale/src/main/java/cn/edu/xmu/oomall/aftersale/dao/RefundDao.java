package cn.edu.xmu.oomall.aftersale.dao;

import cn.edu.xmu.oomall.aftersale.mapper.RefundPoMapper;
import cn.edu.xmu.oomall.aftersale.mapper.po.ExpressPo;
import cn.edu.xmu.oomall.aftersale.mapper.po.RefundPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import org.springframework.stereotype.Repository;

import java.util.List;

@RefreshScope
@Slf4j
@RequiredArgsConstructor
@Repository
public class RefundDao {
    private final RefundPoMapper refundPoMapper;

    //增加退款单记录
    public void save(RefundPo refundPo){
        refundPoMapper.save(refundPo);
    }

    public List<RefundPo> findByAftersaleOrderId(Long aftersaleOrderId){
        log.info("根据售后单id查找快递信息: {}", aftersaleOrderId);
        return refundPoMapper.findByAftersaleOrderId(aftersaleOrderId);
    }
}
