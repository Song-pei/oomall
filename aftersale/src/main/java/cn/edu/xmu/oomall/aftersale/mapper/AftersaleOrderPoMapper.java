package cn.edu.xmu.oomall.aftersale.mapper;

import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import org.glassfish.jaxb.core.v2.model.core.ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AftersaleOrderPoMapper extends JpaRepository<AftersaleOrderPo, Long>, JpaSpecificationExecutor<AftersaleOrderPo> {
    //AftersaleOrderPo findById(ID Id);
}
