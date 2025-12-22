package cn.edu.xmu.oomall.service.mapper;

import cn.edu.xmu.oomall.service.mapper.po.ServiceProviderPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderPoMapper extends JpaRepository<ServiceProviderPo, Long>, JpaSpecificationExecutor<ServiceProviderPo> {
}
