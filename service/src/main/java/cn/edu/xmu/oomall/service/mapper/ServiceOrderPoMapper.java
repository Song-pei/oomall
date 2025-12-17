package cn.edu.xmu.oomall.service.mapper;

import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ServiceOrderPoMapper extends JpaRepository<ServiceOrderPo, Long> {

    List<ServiceOrderPo> findByShopId(Long shopId, Pageable pageable);

    List<ServiceOrderPo> findByShopIdEqualsAndStatusEquals(Long shopId, Byte status, Pageable pageable);

    List<ServiceOrderPo> findByMaintainerIdEqualsAndStatusEquals(Long maintainerId, Byte status, Pageable pageable);

}