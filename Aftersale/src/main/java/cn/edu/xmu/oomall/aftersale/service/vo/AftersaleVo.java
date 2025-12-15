package cn.edu.xmu.oomall.aftersale.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.aftersale.dao.bo.Aftersale;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@CopyFrom({Aftersale.class})
//@CopyNotNullTo({AftersaleVo.class})
@Getter
public class AftersaleVo {
    @Setter
    private Long id;
    @Setter
    private Long orderItemId;
    @Setter
    private Long customerId;
    @Setter
    private Long shopId;
    @Setter
    private String aftersaleSn;
    @Setter
    private Byte type;
    @Setter
    private String reason;
    @Setter
    private String conclusion;
    @Setter
    private Long quantity;
    @Setter
    private Long regionId;
    @Setter
    private String address;
    @Setter
    private String consignee;
    @Setter
    private String mobile;
    @Setter
    private Byte status;
    @Setter
    private Long serviceId;
    @Setter
    private String serialNo;
    @Setter
    private String name;
    @Setter
    private Long creatorId;
    @Setter
    private String creatorName;
    @Setter
    private Long modifierId;
    @Setter
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    @Setter
    private Byte inArbitrated;

    public void setGmtCreate(LocalDateTime gmtCreate) {
        if (Objects.nonNull(gmtCreate)) {
            this.gmtCreate = gmtCreate.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
        }
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        if (Objects.nonNull(gmtModified)) {
            this.gmtModified = gmtModified.atZone(LocaleContextHolder.getTimeZone().toZoneId()).toLocalDateTime();
        }
    }
}
