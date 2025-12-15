//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.infrastructure.mapper;

import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegionTemplatePoMapper extends JpaRepository<RegionTemplatePo, Long> {

    @Query("select rpo from RegionTemplatePo rpo join TemplateRegionPo region on rpo.id = region.regionTemplateId where rpo.templateId = :tid and region.regionId = :rid")
    Optional<RegionTemplatePo> findByTemplateIdAndRegionId(Long tid,Long rid);
    List<RegionTemplatePo> findByTemplateId(Long tid, Pageable pageable);
    List<RegionTemplatePo> findByTemplateId(Long id);
    void deleteAllByTemplateId(Long tid);
}
