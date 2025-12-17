package cn.edu.xmu.oomall.aftersale.service;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
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
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize, UserToken user) {
        return aftersaleOrderDao.searchAftersales(shopId, aftersaleSn, orderSn, status, type, applyTime, page, pageSize, user);
    }

    public void audit(Long shopId, Long id,
                      Boolean confirm,
                      String conclusion,
                      String reason,
                      UserToken user) {

        log.info("开始审核售后单: shopId={}, id={}, confirm={}, user={}", shopId, id, confirm, user);

        // 1. 查出 PO
        AftersaleOrderPo po = aftersaleOrderDao.findById(id);
        if (po == null) {
            log.warn("审核失败: 售后单不存在, id={}", id);
            throw new BusinessException(ReturnNo.AGTSERSALE_NOT_EXIST, id);
        }

        // 2. 转为 BO
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);

        // 3. 校验
        if (!Objects.equals(bo.getShopId(), shopId)) {
            log.warn("审核失败: 店铺不匹配, id={}, shopId={}, targetShopId={}", id, bo.getShopId(), shopId);
            throw new BusinessException(ReturnNo.SHOP_NOT_OWN_AFTERSALE, shopId, id);
        }
        if (bo.getStatus() != 0) {
            log.warn("审核失败: 状态不正确, id={}, currentStatus={}", id, bo.getStatus());
            throw new BusinessException(ReturnNo.AFTERSALE_STATUS_NOT_APPLICABLE);
        }

        // 4. 执行 BO 业务逻辑
        // 这行代码负责修改 status, conclusion, reason
        bo.audit(conclusion, reason, Boolean.TRUE.equals(confirm));

        // 5. 先同步业务数据，再填审计信息
        // 将 BO 改后的状态同步回 PO
        po = CloneFactory.copy(po, bo);

        // 6. 填充审计字段 (修改人)
        if (user != null) {
            po.setModifierId(user.getId());
            po.setModifierName(user.getName());
        } else {
            // 兜底
            po.setModifierId(0L);
            po.setModifierName("System");
        }

        // 显式更新时间
        po.setGmtModified(java.time.LocalDateTime.now());


        // 7. 持久化
        aftersaleOrderDao.update(po);

        log.info("po after update: {}", po);
        log.info("[Service] 审核完成: boId={}, 结果={}, reason={}", id, confirm, reason);
    }
}
