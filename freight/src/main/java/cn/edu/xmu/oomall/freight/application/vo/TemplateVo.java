package cn.edu.xmu.oomall.freight.application.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyFrom({Template.class})
@Getter
public class TemplateVo {
    @Setter
    private Long id;
    @Setter
    private Long shopId;
    @Setter
    private String name;
    @Setter
    private Byte defaultModel;
    @Setter
    private String divideStrategy;
    @Setter
    private String packAlgorithm;
    @Setter
    private String creator;
    @Setter
    private String modifier;

    private LocalDateTime gmtCreate;
    public void setGmtCreate(Instant gmtCreate) {
        this.gmtCreate = gmtCreate.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
    }
    private LocalDateTime gmtModified;
    public void setGmtModified(Instant gmtModified) {
        this.gmtModified = gmtModified.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
    }
}
