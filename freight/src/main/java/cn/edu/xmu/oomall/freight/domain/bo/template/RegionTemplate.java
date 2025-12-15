//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.freight.domain.TemplateRepository;
import cn.edu.xmu.oomall.freight.domain.bo.Region;
import cn.edu.xmu.oomall.freight.domain.RegionRepository;
import cn.edu.xmu.oomall.freight.domain.bo.ProductItem;
import cn.edu.xmu.oomall.freight.domain.template.TemplateRegionRepository;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.PieceTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.RegionTemplatePo;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.WeightTemplatePo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.Constants.PLATFORM;

/**
 * 运费模板的父类
 */
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@CopyNotNullTo({RegionTemplatePo.class, WeightTemplatePo.class, PieceTemplatePo.class})
public abstract class RegionTemplate implements Cloneable, Serializable {

    @Getter
    @Setter
    protected Long id;

    /**
     * 创建者
     */
    @Getter
    @Setter
    protected String creator;

    /**
     * 修改者
     */
    @Getter
    @Setter
    protected String modifier;

    /**
     * 创建时间
     */
    @Getter
    @Setter
    protected Instant gmtCreate;

    /**
     * 修改时间
     */
    @Getter
    @Setter
    protected Instant gmtModified;
    /**
     * 包裹的件数上限
     */
    @Getter
    @Setter
    protected Integer upperLimit;

    /**
     * 续重或续件计算单位 克或个
     */
    @Getter
    @Setter
    protected Integer unit;

    @Getter
    @Setter
    protected String objectId;

    @Setter
    protected List<Long> regionIds;

    @JsonIgnore
    @ToString.Exclude
    @Setter
    private TemplateRegionRepository templateRegionRepository;

    public List<Long> getRegionIds() {
        if (Objects.isNull(regionIds) && Objects.nonNull(this.templateRegionRepository)) {
            this.regionIds = this.templateRegionRepository.findRegionIdsByRegionTemplateId(this.getId());
        }
        return this.regionIds;
    }

    @JsonIgnore
    @ToString.Exclude
    private List<Region> regions;

    public List<Region> getRegions(){
        if (Objects.nonNull(this.regionRepository) && Objects.nonNull(regionIds) && Objects.isNull(regions)){
            this.regions = this.getRegionIds().stream().map(id -> {
                try {
                    Region region = this.regionRepository.findById(id);
                    return region;
                }catch (BusinessException e){
                    if (ReturnNo.RESOURCE_ID_NOTEXIST.equals(e.getErrno())){
                        return null;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
        }
        return this.regions;
    }

    @Getter
    @Setter
    protected Long templateId;

    @Setter
    @JsonIgnore
    @ToString.Exclude
    protected TemplateRepository templateRepository;

    @JsonIgnore
    @ToString.Exclude
    protected Template template;

    /**
     * @return
     */
    public Template getTemplate() {
        if (Objects.isNull(this.template) && (Objects.nonNull(this.templateRepository))) {
            this.template = this.templateRepository.findById(PLATFORM, this.templateId).orElse(null);
        }
        return this.template;
    }

    @Setter
    @JsonIgnore
    @ToString.Exclude
    protected RegionRepository regionRepository;

    /**
     * 计算包裹运费
     *
     * @param productItems
     * @return
     */
    public Collection<TemplateResult> calculate(Collection<ProductItem> productItems) {
        return this.getTemplate().getStrategy().divide(this, productItems).stream().map(pack -> {
            Long fee = cacuFreight(pack);
            return new TemplateResult(fee, pack);
        }).collect(Collectors.toList());
    }

    /**
     * 根据包裹里的商品计算运费
     *
     * @param pack
     * @return
     * @author Ming Qiu
     * <p>
     * date: 2022-11-20 15:54
     */
    public abstract Long cacuFreight(Collection<ProductItem> pack);

    public TemplateType gotType() {
        return this.getTemplate().gotType();
    }

}
