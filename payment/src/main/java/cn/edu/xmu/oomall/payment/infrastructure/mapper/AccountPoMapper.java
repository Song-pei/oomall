package cn.edu.xmu.oomall.payment.infrastructure.mapper;

import cn.edu.xmu.oomall.payment.infrastructure.mapper.po.AccountPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountPoMapper extends JpaRepository<AccountPo, Long> {
}
