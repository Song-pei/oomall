package cn.edu.xmu.oomall.product.repository.openfeign;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.FreightClient;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.po.Freight;
import cn.edu.xmu.oomall.product.infrastructure.openfeign.po.Logistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

import static cn.edu.xmu.javaee.core.model.Constants.MAX_RETURN;
/**
 *
 * @author jyx
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class FreightRepository {
    private  final FreightClient freightClient;

    public Logistics GetLogistics(Long shopId, Long shopLogisticsId) {
        InternalReturnObject<Freight> ret = this.freightClient.getAllLogisticsById(shopId, 0, MAX_RETURN);
        List<Logistics> logistics = ret.getData().getLogistics();
        Optional<Logistics> foundLogistics = logistics.stream().filter(obj -> obj.getId() == shopLogisticsId).findFirst();

        if (foundLogistics.isPresent()) {
            return foundLogistics.get();
        }
        else {
            throw new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST,ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage());
        }
    }
}
