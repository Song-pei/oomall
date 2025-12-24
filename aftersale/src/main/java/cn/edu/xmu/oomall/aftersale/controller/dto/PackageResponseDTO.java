package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponseDTO {

    private Long expressId;

    // 修改这里：必须叫 billCode，对应物流模块 SimpleExpressVo 的 billCode
    private String billCode;
}