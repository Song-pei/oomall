package cn.edu.xmu.oomall.aftersale.dao;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.AftersaleOrderPoMapper;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RefreshScope
@Slf4j
@RequiredArgsConstructor
@Repository
public class AftersaleOrderDao {
    private final AftersaleOrderPoMapper aftersaleOrderPoMapper;

    /**
     * 分页查询所有售后单
     */
    public List<AftersaleOrder> searchAftersales(Long shopId, String aftersaleSn, String orderSn, Integer status, Integer type, String applyTime, Integer page, Integer pageSize) {
        Specification<AftersaleOrderPo> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (shopId != null) {
                predicates.add(criteriaBuilder.equal(root.get("shopId"), shopId));
            }
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
                    // Ignore invalid date format
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
    public void update(AftersaleOrderPo po) {
        aftersaleOrderPoMapper.save(po);
    }

}
