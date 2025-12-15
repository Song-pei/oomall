package cn.edu.xmu.oomall.logistics.mapper.jpa;

import cn.edu.xmu.oomall.logistics.mapper.po.LogisticsPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author fan ninghan
 * 2023-dng3-008
 */
@Repository
public interface LogisticsPoMapper extends JpaRepository<LogisticsPo, Long> {
}
