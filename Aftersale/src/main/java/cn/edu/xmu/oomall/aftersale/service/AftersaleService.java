package cn.edu.xmu.oomall.aftersale.service;

import cn.edu.xmu.oomall.aftersale.dao.AftersaleDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.Aftersale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class AftersaleService {
    private final AftersaleDao aftersaleDao;

    /**
     * 分页查询所有售后单
     */
    public List<Aftersale> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize) {
        return aftersaleDao.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize);
    }
}
