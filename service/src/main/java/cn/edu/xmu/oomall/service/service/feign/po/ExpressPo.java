package cn.edu.xmu.oomall.service.service.feign.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ExpressPo {
    private Long id;
    private String billCode;
}