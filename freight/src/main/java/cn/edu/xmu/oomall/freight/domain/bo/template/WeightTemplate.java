package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.WeightTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.WeightThresholdPo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyFrom({WeightTemplatePo.class, RegionTemplatePo.class})
@CopyNotNullTo({WeightTemplatePo.class, RegionTemplatePo.class})
@Slf4j
public class WeightTemplate extends RegionTemplate implements Serializable {

    /**
     * 首重
     */
    private Integer firstWeight;

    /**
     * 首重以下均为此费用
     */
    private Long firstWeightPrice;

    private List<WeightThresholdPo> thresholds;

    @Override
    public Long cacuFreight(Collection<ProductItem> pack) {
        Long result = this.firstWeightPrice;
        Integer weight = pack.stream().map(item -> item.getWeight() * item.getQuantity()).reduce((x, y) -> x + y).get();
        log.debug("cacuFreight: weight = {}", weight);
        Integer prevThreshold = this.firstWeight;
        if (weight - this.firstWeight > 0) {
            for (WeightThresholdPo threshold : this.thresholds) {
                Integer upper = weight - threshold.getBelow() > 0 ? threshold.getBelow() : weight;
                //计算有多少个计价单位
                long num = (upper - prevThreshold) / this.unit;
                log.debug("cacuFreight: upper = {}, prevThreshold = {},  {}", upper, prevThreshold, (upper - prevThreshold) % this.unit);
                if (0 != ((upper - prevThreshold) % this.unit)) {
                    num += 1;
                }
                result += threshold.getPrice() * num;
                prevThreshold = threshold.getBelow();
                log.debug("cacuFreight: result = {}, threshold = {}, num = {}", result, threshold, num);
                if (upper == weight) {
                    break;
                }
            }
        }
        return result;
    }
}
