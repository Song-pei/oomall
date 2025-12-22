package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建运单的请求参数 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageCreateDTO {

    private Contact sender;
    private Contact delivery;
    private Long shopLogisticId;
    private String goodsType;
    // 物流端 ExpressDto 里的 weight 是 Long 类型，这里建议同步修改
    private Long weight;
    private Integer payMethod;

    /**
     * 将名字改为与物流端结构更接近的定义
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