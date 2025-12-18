package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应服务模块的 SimpleServiceVo
 */
@Data
@NoArgsConstructor
public class ServiceOrderResponseDTO {

    private Long id;

    private Integer type;

    private String consignee;
}