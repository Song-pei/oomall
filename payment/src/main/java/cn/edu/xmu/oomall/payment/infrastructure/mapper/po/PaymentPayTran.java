package cn.edu.xmu.oomall.payment.infrastructure.mapper.po;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment_pay_trans", schema = "payment")
public class PaymentPayTran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 128)
    @Column(name = "out_no", length = 128)
    private String outNo;

    @Size(max = 128)
    @Column(name = "trans_no", length = 128)
    private String transNo;

    @ColumnDefault("'0'")
    @Column(name = "amount", columnDefinition = "int UNSIGNED not null")
    private Long amount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "status", nullable = false)
    private Byte status;

    @Column(name = "success_time")
    private Instant successTime;

    @Size(max = 128)
    @Column(name = "sp_openid", length = 128)
    private String spOpenid;

    @Column(name = "time_expire")
    private Instant timeExpire;

    @Column(name = "time_begin")
    private Instant timeBegin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private PaymentAccount account;

    @Size(max = 128)
    @Column(name = "creator", length = 128)
    private String creator;

    @Size(max = 128)
    @Column(name = "modifier", length = 128)
    private String modifier;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "gmt_create", nullable = false)
    private Instant gmtCreate;

    @Column(name = "gmt_modified")
    private Instant gmtModified;

    @Size(max = 128)
    @Column(name = "prepay_id", length = 128)
    private String prepayId;

    @ColumnDefault("'0'")
    @Column(name = "div_amount", columnDefinition = "int UNSIGNED not null")
    private Long divAmount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "in_refund", nullable = false)
    private Byte inRefund;

    @Column(name = "shop_id", columnDefinition = "int UNSIGNED not null")
    private Long shopId;

    @Size(max = 128)
    @Column(name = "description", length = 128)
    private String description;

}