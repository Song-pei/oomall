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
@Table(name = "freight_region_template")
public class RegionTemplatePo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "template_id", columnDefinition = "int UNSIGNED")
    private Long templateId;

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
    @Column(name = "object_id", length = 128)
    private String objectId;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "unit", nullable = false)
    private Integer unit;

    @NotNull
    @ColumnDefault("10000")
    @Column(name = "upper_limit", nullable = false)
    private Integer upperLimit;

}