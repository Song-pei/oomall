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
@Table(name = "payment_channel", schema = "payment")
public class PaymentChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 128)
    @NotNull
    @Column(name = "sp_appid", nullable = false, length = 128)
    private String spAppid;

    @Size(max = 128)
    @NotNull
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 128)
    @NotNull
    @Column(name = "sp_mchid", nullable = false, length = 128)
    private String spMchid;

    @Column(name = "begin_time")
    private Instant beginTime;

    @Column(name = "end_time")
    private Instant endTime;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "status", nullable = false)
    private Byte status;

    @Size(max = 256)
    @Column(name = "bean_name", length = 256)
    private String beanName;

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

}