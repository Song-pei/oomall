//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.aftersale.service.vo;

import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundTransVo {
    private Long id;
    private String outNo;
    private String transNo;
    private Long amount;
    private Long divAmount;
    private LocalDateTime successTime;
    private SimpleChannelVo channel;
    private Byte status;
    private String userReceivedAccount;
    private IdNameTypeVo creator;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private IdNameTypeVo modifier;

    private SimpleTransVo payTrans;

    private SimpleTransVo divTrans;
    private IdNameTypeVo adjustor;
    private LocalDateTime adjustTime;

    private LedgerVo ledger;


}
