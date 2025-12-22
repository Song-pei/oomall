package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建运单的请求参数 DTO
 */
@Data
@Builder //
@NoArgsConstructor
@AllArgsConstructor
public class PackageCreateDTO {

    private Contact sender;       // 寄件人
    private Contact delivery;     // 收件人
    private Long shopLogisticId;  // 商家物流ID
    private String goodsType;     // 物品类型
    private Integer weight;       // 重量
    private Integer payMethod;    // 支付方式

    /**
     * 联系人内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private Long regionId;
        private String address;
        private String mobile;
        private String name;
    }
}