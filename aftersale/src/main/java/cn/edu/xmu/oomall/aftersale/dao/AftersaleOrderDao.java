package cn.edu.xmu.oomall.aftersale.dao;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.AftersaleOrderPoMapper;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
// 假设这是你的 UserToken 包路径，请根据实际情况调整
import cn.edu.xmu.javaee.core.model.UserToken;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RefreshScope
@Slf4j
@RequiredArgsConstructor
@Repository
public class AftersaleOrderDao {

    private final AftersaleOrderPoMapper aftersaleOrderPoMapper;

    /**
     * 分页查询所有售后单
     * 增加 UserToken 参数用于权限隔离
     */
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn,
                                                 Integer status, Integer type, String applyTime,
                                                 Integer page, Integer pageSize,
                                                 UserToken user) {

        log.debug("DAO查询售后单: shopId={}, status={}, page={}, user={}", shopId, status, page, user);
        Specification<AftersaleOrderPo> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 如果用户存在且 departId 不为 0 (说明是商家)
            if (user != null && user.getDepartId() != null && user.getDepartId() != 0L) {
                // 强制只查该商家自己的数据 (覆盖掉前端传来的 shopId)
                predicates.add(criteriaBuilder.equal(root.get("shopId"), user.getDepartId()));
            } else {
                // 如果是平台管理员 (departId == 0) 或者 测试环境
                // 允许使用前端传来的 shopId 进行筛选
                if (shopId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("shopId"), shopId));
                }
            }
            // --- 权限控制逻辑 END ---

            if (aftersaleSn != null && !aftersaleSn.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("aftersaleSn"), aftersaleSn));
            }

            // 注意：orderSn 不在 aftersale_aftersale 表中，此查询条件将失效
            // if (orderSn != null && !orderSn.isBlank()) {
            //     predicates.add(criteriaBuilder.equal(root.get("orderSn"), orderSn));
            // }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.byteValue()));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type.byteValue()));
            }

            if (applyTime != null && !applyTime.isBlank()) {
                try {
                    LocalDate date = LocalDate.parse(applyTime, DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                    predicates.add(criteriaBuilder.between(root.get("gmtCreate"), startOfDay, endOfDay));
                } catch (Exception e) {
                    // 抛出字段校验错误异常
                    log.warn("日期格式解析错误: {}", applyTime);
                    throw new BusinessException(ReturnNo.FIELD_NOTVALID, "日期格式错误，正确格式: yyyy-MM-dd");
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AftersaleOrderPo> poPage = aftersaleOrderPoMapper.findAll(spec, PageRequest.of(page - 1, pageSize));

        return poPage.getContent().stream()
                .map(po -> CloneFactory.copy(new AftersaleOrder(), po))
                .collect(Collectors.toList());
    }

    public AftersaleOrderPo findById(Long id) {
        return aftersaleOrderPoMapper.findById(id).orElse(null);
    }

    /**
     * 审核更新方法
     * 包含：校验、状态更新、审计字段填充(User)
     */
    public void audit(Long shopId, Long id, Boolean confirm, String conclusion, String reason, UserToken user) {
        // 1. 查询
        AftersaleOrderPo po = this.findById(id);
        if (po == null) {
            log.warn("DAO审核失败: 单据不存在 id={}", id);
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        // 2. 权限校验 (防止商家A操作商家B的数据)
        if (user != null && user.getDepartId() != 0L && !po.getShopId().equals(user.getDepartId())) {
            log.warn("DAO审核失败: 越权操作 userShopId={}, targetShopId={}", user.getDepartId(), po.getShopId());
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }

        // 3. 更新业务字段
        Integer newStatus = Boolean.TRUE.equals(confirm) ? 3 : 6;// 3审核通过已生成服务单，6审核不通过，取消
        po.setStatus(newStatus);
        po.setConclusion(conclusion);
        po.setReason(reason);

        // 4. 修改人
        if (user != null) {
            po.setModifierId(user.getId());
            po.setModifierName(user.getName());
        } else {
            po.setModifierId(0L);
            po.setModifierName("System");
        }
        po.setGmtModified(LocalDateTime.now());

        aftersaleOrderPoMapper.save(po);
        log.info("DAO审核完成: id={}, status={}, modifier={}", id, newStatus, po.getModifierName());
    }

    /**
     * 更新方法
     */
    public void update(AftersaleOrderPo po) {
        log.debug("DAO执行更新: id={}, po={}", po.getId(), po);
        aftersaleOrderPoMapper.save(po);
    }
}