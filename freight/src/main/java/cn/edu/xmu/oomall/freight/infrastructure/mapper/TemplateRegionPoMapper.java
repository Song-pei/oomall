package cn.edu.xmu.oomall.freight.infrastructure.mapper;

import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.TemplateRegionPo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRegionPoMapper extends JpaRepository<TemplateRegionPo, Long> {
    List<TemplateRegionPo> findByRegionTemplateId(Long regionTemplateId);
    void deleteAllByRegionTemplateId(Long regionTemplateId);
}
