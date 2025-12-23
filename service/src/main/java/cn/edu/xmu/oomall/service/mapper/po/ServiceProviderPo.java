package cn.edu.xmu.oomall.service.mapper.po;


import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_provider")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@CopyFrom({ServiceProvider.class})
public class ServiceProviderPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String introduction;
    private LocalDateTime applyTime;
    private String consignee;
    private String address;
    private String mobile;
    private String category;
    private String qualification;
    private String username;
    private String legalRep;
    private Byte status;
    private Long regionId;

}
