//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.aftersale.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.aftersale.service.vo.PayTransVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 退款Vo
 */
@Data
@NoArgsConstructor
@CopyFrom(PayTransVo.class)
public class RefundTransDto {
    @NotNull(message =  "退款金额不能为空")
    @Min(value = 1, message = "退款金额需大于0")
    private Long amount;

    @NotNull(message =  "分账金额不能为空")
    @Min(value = 0, message = "退回分账金额需大于等于0")
    private Long divAmount;

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getDivAmount() {
        return divAmount;
    }

    public void setDivAmount(Long divAmount) {
        this.divAmount = divAmount;
    }
}
