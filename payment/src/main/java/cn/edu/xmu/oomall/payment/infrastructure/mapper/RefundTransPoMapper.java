package cn.edu.xmu.oomall.payment.infrastructure.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import cn.edu.xmu.oomall.payment.infrastructure.mapper.po.RefundTransPo;

public interface RefundTransPoMapper extends JpaRepository<RefundTransPo, Long> {
}
