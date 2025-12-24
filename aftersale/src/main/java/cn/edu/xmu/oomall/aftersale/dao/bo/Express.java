package cn.edu.xmu.oomall.aftersale.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.mapper.po.ExpressPo;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Data
@NoArgsConstructor
@CopyTo(ExpressPo.class)
@CopyFrom({ExpressPo.class,PackageResponseDTO.class})
public class Express {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long expressId;
    private String billCode;
    private Long aftersaleOrderId;

    private Integer direction;
    private Integer type;
    private Byte status;

    private Long creatorId;
    private String creatorName;
    private Long modifierId;
    private String modifierName;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;

    public void setModifier(UserToken user) {
        if (user != null) {
            this.setModifierId(user.getId());
            this.setModifierName(user.getName());
        } else {
            this.setModifierId(0L);
            this.setModifierName("System");
        }
        this.setGmtModified(LocalDateTime.now());
    }
}
