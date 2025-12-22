package cn.edu.xmu.oomall.service.dao.bo;


import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.service.dao.ServiceProviderDao;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import cn.edu.xmu.oomall.service.mapper.po.ServiceProviderPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@CopyFrom(ServiceProviderPo.class)
public class ServiceProvider extends OOMallObject implements Serializable {
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

    @JsonIgnore
    @Setter
    ServiceProviderDao serviceProviderDao;
    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

}
