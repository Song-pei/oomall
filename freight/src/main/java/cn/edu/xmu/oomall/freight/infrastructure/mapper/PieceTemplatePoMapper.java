//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.infrastructure.mapper;

import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.PieceTemplatePo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface PieceTemplatePoMapper extends MongoRepository<PieceTemplatePo, String> {
}
