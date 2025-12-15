//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.adapter.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@CopyTo({ProductItem.class})
@Data
public class ProductItemDto {

    @NotNull(message = "orderItemId不能为空")
    private Long orderItemId;

    @NotNull(message = "productId不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private Integer weight;

}

