package cn.edu.xmu.oomall.freight.infrastructure.mapper.po;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "freight_region")
public class TemplateRegionPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "region_id", columnDefinition = "int UNSIGNED not null")
    private Long regionId;

    @Column(name = "region_template_id", columnDefinition = "int UNSIGNED not null")
    private Long regionTemplateId;

}