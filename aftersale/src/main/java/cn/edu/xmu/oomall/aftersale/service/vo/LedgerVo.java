//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.aftersale.service.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LedgerVo {

    private Long id;
    private String outNo;
    private String transNo;
    private Long amount;
    private LocalDateTime checkTime;

}
