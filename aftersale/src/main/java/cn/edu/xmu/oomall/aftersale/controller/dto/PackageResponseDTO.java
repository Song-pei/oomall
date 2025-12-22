package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageResponseDTO {
    private Long id;
    private String expressNo; // 运单号
}
