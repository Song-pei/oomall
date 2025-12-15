package cn.edu.xmu.oomall.region.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.oomall.region.dao.bo.Region;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 地区的视图对象
 * 用于向前端返回值
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@CopyFrom({Region.class})
@Getter
public class RegionVo {
    @Setter
    private Long id;
    @Setter
    private String name;
    @Setter
    private Byte status;
    @Setter
    private Byte level;
    @Setter
    private String shortName;
    @Setter
    private String mergerName;
    @Setter
    private String pinyin;
    @Setter
    private Double lng;
    @Setter
    private Double lat;
    @Setter
    private String areaCode;
    @Setter
    private String zipCode;
    @Setter
    private String cityCode;
    @Setter
    private String creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    @Setter
    private String modifier;

    public void setGmtCreated(Instant gmtCreate) {
        if (Objects.nonNull(gmtCreate)) {
            this.gmtCreate = gmtCreate.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
        }
    }

    public void setGmtModified(Instant gmtModified) {
        if (Objects.nonNull(gmtModified)) {
            this.gmtModified = gmtModified.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
        }
    }
}
