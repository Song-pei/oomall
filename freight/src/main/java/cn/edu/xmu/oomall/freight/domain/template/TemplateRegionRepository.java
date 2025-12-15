package cn.edu.xmu.oomall.freight.domain.template;

import cn.edu.xmu.oomall.freight.infrastructure.mapper.TemplateRegionPoMapper;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.TemplateRegionPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class TemplateRegionRepository {

    private final TemplateRegionPoMapper templateRegionPoMapper;

    public List<Long> findRegionIdsByRegionTemplateId(Long regionTemplateId) {
        return this.templateRegionPoMapper.findByRegionTemplateId(regionTemplateId).stream().map(TemplateRegionPo::getRegionId).collect(Collectors.toList());
    }
}
