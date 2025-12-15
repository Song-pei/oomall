//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.payment.service.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.oomall.payment.domain.bo.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@CopyFrom({Transaction.class, DivPayTrans.class, DivRefundTrans.class, PayTrans.class})
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

    public SimpleTransVo(Transaction trans){
        super();
        CloneFactory.copy(this, trans);
        this.chanel = CloneFactory.copy(new SimpleChannelVo(), trans.getChannel());
    }
}
