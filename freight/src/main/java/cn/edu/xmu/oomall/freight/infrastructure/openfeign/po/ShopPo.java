//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.infrastructure.openfeign.po;


import lombok.Data;

@Data
public class ShopPo {

    private Long id;

    /**
     * 商铺名称
     */
    private String name;

    /**
     * 状态
     */
    private Byte status;
}
