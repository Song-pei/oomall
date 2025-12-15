package cn.edu.xmu.oomall.freight.application;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.freight.domain.TemplateRepository;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class TemplateReuseService {

    private final TemplateRepository templateRepository;
    /**
     * 获得运费模板详情
     * @param shopId
     * @param id
     */
    public Template findTemplateById(Long shopId, Long id){
        Template template = this.templateRepository.findById(shopId, id).orElseThrow(()->new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, String.format(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage(), "运费模板", id)));
        if (!template.getShopId().equals(shopId) && !shopId.equals(PLATFORM)) {
            throw new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format(ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage(), "模板", template.getId(), shopId));
        }
        return template;
    }
}
