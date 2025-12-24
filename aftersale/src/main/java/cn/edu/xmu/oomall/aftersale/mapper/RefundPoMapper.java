package cn.edu.xmu.oomall.aftersale.mapper;
import cn.edu.xmu.oomall.aftersale.mapper.po.ExpressPo;
import cn.edu.xmu.oomall.aftersale.mapper.po.RefundPo;
import org.glassfish.jaxb.core.v2.model.core.ID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundPoMapper extends JpaRepository<RefundPo,Integer>, JpaSpecificationExecutor<RefundPo> {
    List<RefundPo> findByAftersaleOrderId(Long aftersaleOrderId);
}
