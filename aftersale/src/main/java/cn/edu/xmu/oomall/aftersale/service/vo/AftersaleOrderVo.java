package cn.edu.xmu.oomall.aftersale.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
//@CopyNotNullTo({AftersaleVo.class})
@Getter
@CopyFrom({AftersaleOrder.class})
public class AftersaleOrderVo {
    @Setter
    private Long id;
    @Setter
    private Long shopId;
    @Setter
    private Long customerId;
    @Setter
    private Long orderId;
    @Setter
    private Integer type;
    @Setter
    private Integer status;
    @Setter
    private String conclusion;
    @Setter
    private String reason;
    @Setter
    private LocalDateTime createTime;
    @Setter
    private LocalDateTime updateTime;

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
