//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.aftersale.service.vo;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SimpleTransVo {
    private Long id;
    private String outNo;
    private String transNo;
    private Long amount;
    private Byte status;
    private LocalDateTime successTime;
    private SimpleChannelVo chanel;

    private IdNameTypeVo adjustor;

    private LocalDateTime adjustTime;

    private LedgerVo ledger;

}
