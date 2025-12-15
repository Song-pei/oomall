package cn.edu.xmu.oomall.region.mapper.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "region_region")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class RegionPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pid;
    private Byte level;
    private String areaCode;
    private String zipCode;
    private String cityCode;
    private String name;
    private String shortName;
    private String mergerName;
    private String pinyin;
    private Double lng;
    private Double lat;
    private Byte status;
    private String creator;
    private String modifier;
    private Instant gmtCreate;
    private Instant gmtModified;
}
