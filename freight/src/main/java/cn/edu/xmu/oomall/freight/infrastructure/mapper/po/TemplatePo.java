package cn.edu.xmu.oomall.freight.infrastructure.mapper.po;

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
@Table(name = "freight_template")
public class TemplatePo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "shop_id", columnDefinition = "int UNSIGNED not null")
    private Long shopId;

    @Size(max = 128)
    @Column(name = "name", length = 128)
    private String name;

    @ColumnDefault("0")
    @Column(name = "default_model")
    private Byte defaultModel;

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

    @Size(max = 64)
    @NotNull
    @Column(name = "template_bean", nullable = false, length = 64)
    private String templateBean;

    @Size(max = 64)
    @Column(name = "divide_strategy", length = 64)
    private String divideStrategy;

    @Size(max = 64)
    @Column(name = "pack_algorithm", length = 64)
    private String packAlgorithm;

}