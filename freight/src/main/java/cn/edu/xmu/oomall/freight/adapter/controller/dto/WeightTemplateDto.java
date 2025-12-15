//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.adapter.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.freight.domain.bo.template.RegionTemplate;
import cn.edu.xmu.oomall.freight.domain.bo.template.WeightTemplate;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.WeightThresholdPo;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@ToString(callSuper = true, doNotUseGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
@CopyTo({WeightTemplate.class, RegionTemplate.class})
public class WeightTemplateDto{

    @Min(value = 1, message = "计量单位至少为1")
    private Integer unit;

    @Min(value = 1, message = "数量上限至少为1")
    private Integer upperLimit;

    private Integer firstWeight;

    private Long firstWeightFreight;

    private List<WeightThresholdPo> thresholds;

    private List<Long> regionIds;
}
