package cn.edu.xmu.oomall.freight.adapter.controller.dto;


import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.freight.domain.bo.template.PieceTemplate;
import cn.edu.xmu.oomall.freight.domain.bo.template.RegionTemplate;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
@CopyTo({PieceTemplate.class, RegionTemplate.class})
public class PieceTemplateDto{

    @Min(value = 1, message = "计量单位至少为1")
    private Integer unit;

    @Min(value = 1, message = "数量上限至少为1")
    private Integer upperLimit;

    private Integer firstItem;

    private Long firstItemPrice;

    private Integer additionalItems;

    private Long additionalItemsPrice;

    private List<Long> regionIds;
}
