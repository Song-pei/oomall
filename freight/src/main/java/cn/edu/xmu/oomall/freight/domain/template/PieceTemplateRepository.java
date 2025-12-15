//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.domain.template;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.freight.domain.bo.template.PieceTemplate;
import cn.edu.xmu.oomall.freight.domain.bo.template.RegionTemplate;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.PieceTemplatePoMapper;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.PieceTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PieceTemplateRepository implements TemplateRepositoryInf {

    private final PieceTemplatePoMapper mapper;

    @Override
    public RegionTemplate getRegionTemplate(RegionTemplatePo po) {
        PieceTemplate bo = CloneFactory.copy(new PieceTemplate(), po);
        Optional<PieceTemplatePo> wPo = this.mapper.findById(po.getObjectId());
        log.debug("getRegionTemplate: wPo = {}, ObjectId = {}", wPo, po.getObjectId());
        wPo.ifPresent(templatePo -> {
            CloneFactory.copy(bo, templatePo);
            bo.setObjectId(templatePo.getObjectId());
            log.debug("getRegionTemplate: templatePo = {}, bo = {}", templatePo, bo);
        });
        return bo;
    }

    @Override
    public void update(RegionTemplate bo) {

        PieceTemplatePo savedPo = this.mapper.findById(bo.getObjectId()).orElseThrow(()->new BusinessException(ReturnNo.INCONSISTENT_DATA));
        savedPo = CloneFactory.copyNotNull(savedPo, (PieceTemplate) bo);
        log.debug("savedPo:{}",savedPo);

        PieceTemplatePo retp = this.mapper.save(po);
        log.debug("PieceTemplatePo:{}",retp);
    }

    @Override
    public void delete(String id) throws RuntimeException {
        this.mapper.deleteById(id);
    }

    @Override
    public String insert(RegionTemplate bo) {
        PieceTemplatePo po = CloneFactory.copy(new PieceTemplatePo(), bo);
        PieceTemplatePo newPo = this.mapper.insert(po);
        return newPo.getObjectId();
    }
}
