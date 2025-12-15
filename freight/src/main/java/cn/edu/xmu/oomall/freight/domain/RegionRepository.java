//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.domain;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.freight.domain.bo.Region;
import cn.edu.xmu.oomall.freight.infrastructure.openfeign.RegionClient;
import cn.edu.xmu.oomall.freight.infrastructure.openfeign.po.RegionPo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RegionRepository {

    private final RegionClient regionClient;

    private Region build(RegionPo po) {
        Region bo = CloneFactory.copy(new Region(), po);
        bo.setRegionRepository(this);
        return bo;
    }

    public Region findById(Long id) {
        InternalReturnObject<RegionPo> ret = this.regionClient.findRegionById(id);
        return this.build(ret.getData());
    }

    public List<Region> retrieveParentRegionsById(Long regionId) {
        InternalReturnObject<List<RegionPo>> ret = this.regionClient.retrieveParentRegionsById(regionId);
        return ret.getData().stream().map(po -> this.build(po)).collect(Collectors.toList());
    }

}
