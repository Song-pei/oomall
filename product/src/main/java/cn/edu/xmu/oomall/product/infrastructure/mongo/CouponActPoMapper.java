//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.product.infrastructure.mongo;

import cn.edu.xmu.oomall.product.infrastructure.mapper.po.CouponActPo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponActPoMapper extends MongoRepository<CouponActPo, String> {

}
