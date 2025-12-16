package cn.edu.xmu.oomall.aftersale.service;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@Transactional(propagation = Propagation.REQUIRED)
@RequiredArgsConstructor
@Slf4j
public class AftersaleOrderService {
    private final AftersaleOrderDao aftersaleOrderDao;

    /**
     * 分页查询所有售后单
     */
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize) {
        return aftersaleOrderDao.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize);
    }

    public void audit(Long shopId, Long id,
                      Boolean confirm,
                      String conclusion,
                      String reason) {

        //检验状态
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            throw new IllegalArgumentException("售后单不存在");
        }
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);

        if (!Objects.equals(bo.getShopId(), shopId)) {
            throw new IllegalArgumentException("店铺ID不匹配");
        }
        if (bo.getStatus() != 0) {
            throw new IllegalStateException("只能审核已申请状态的订单");
        }
        // BO 负责处理业务逻辑

        bo.audit(conclusion, reason, Boolean.TRUE.equals(confirm));

        // 将BO的修改更新回PO
        po = CloneFactory.copy(po, bo);
        //持久化
        aftersaleOrderDao.update(po);
        log.info("po after update: {}", po);
        log.info("[Service] 审核完成: boId={}, 结果={}, reason={}", id, confirm, reason);
    }
}
