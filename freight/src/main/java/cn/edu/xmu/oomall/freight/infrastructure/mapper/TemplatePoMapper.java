package cn.edu.xmu.oomall.freight.infrastructure.mapper;

import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.TemplatePo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface TemplatePoMapper extends JpaRepository<TemplatePo, Long>{

    List<TemplatePo> findByShopId(Long shopId, Pageable pageable);

    List<TemplatePo> findByNameAndShopId(String name, Long shopId, Pageable pageable);
}
