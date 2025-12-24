package cn.edu.xmu.oomall.aftersale.dao;
import cn.edu.xmu.oomall.aftersale.dao.bo.Express;
import cn.edu.xmu.oomall.aftersale.mapper.ExpressPoMapper;
import cn.edu.xmu.oomall.aftersale.mapper.po.ExpressPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import org.springframework.stereotype.Repository;

import java.util.List;
@RefreshScope
@Slf4j
@RequiredArgsConstructor
@Repository
public class ExpressDao {

    private final ExpressPoMapper expressPoMapper;

    //增加数据库记录
    public void save(ExpressPo expressPo){
        log.info("插入快递信息: {}", expressPo);
        expressPoMapper.save(expressPo);
    }
    //根据售后单id查找快递信息
    public List<ExpressPo> findByAftersaleOrderId(Long aftersaleOrderId){
        log.info("根据售后单id查找快递信息: {}", aftersaleOrderId);
        return expressPoMapper.findByAftersaleOrderId(aftersaleOrderId);
    }
}
