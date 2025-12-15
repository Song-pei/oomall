//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.PieceTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;


@ToString(callSuper = true, doNotUseGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyFrom({PieceTemplatePo.class, RegionTemplatePo.class})
@CopyNotNullTo({PieceTemplatePo.class})
@Slf4j
@Data
public class PieceTemplate extends RegionTemplate implements Serializable {

    /**
     * 首件数目
     */
    private Integer firstItems;

    /**
     * 起步费用
     */
    private Long firstPrice;

    /**
     * 续件
     */
    private Integer additionalItems;

    /**
     * 每增加additionalItems件商品，增加多少费用,小于additionalItems也是同样价钱
     */
    private Long additionalPrice;

    @Override
    public Long cacuFreight(Collection<ProductItem> pack) {
        Integer total = pack.stream().map(item -> item.getQuantity()).reduce((x, y) -> x + y).get();
        log.debug("cacuFreight: total = {}, template = {}", total, this);
        Long result = this.firstPrice;
        Integer rest = total - this.firstItems;
        if (rest > 0) {
            result += (rest / this.additionalItems) * this.additionalPrice;
            if (0 != rest % this.additionalItems) {
                result += this.additionalPrice;
            }
            log.debug("cacuFreight: result = {}", result);
        }
        return result;
    }
}
