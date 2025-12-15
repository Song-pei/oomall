package cn.edu.xmu.oomall.freight.domain.bo.template;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.oomall.freight.domain.bo.Region;
import cn.edu.xmu.oomall.freight.domain.bo.divide.DivideStrategy;
import cn.edu.xmu.oomall.freight.domain.RegionRepository;
import cn.edu.xmu.oomall.freight.infrastructure.mapper.po.TemplatePo;
import lombok.extern.slf4j.Slf4j;
import cn.edu.xmu.oomall.freight.domain.template.RegionTemplateRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 运费模板对象
 */
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@CopyFrom(TemplatePo.class)
@CopyNotNullTo(TemplatePo.class)
@Slf4j
public class Template implements Serializable, Cloneable {
    /**
     * 默认模板
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Byte DEFAULT = 1;

    @ToString.Exclude
    @JsonIgnore
    public static final Byte COMMON = 0;

    @ToString.Exclude
    @JsonIgnore
    public static final String PIECE = "pieceTemplateDao";

    @ToString.Exclude
    @JsonIgnore
    public static final String WEIGHT = "weightTemplateDao";

    @ToString.Exclude
    @JsonIgnore
    public static Map<String, TemplateType> TYPE = new HashMap<>() {
        {
            put(PIECE, new Piece());
            put(WEIGHT, new Weight());
        }
    };

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
     * 商铺id
     */
    @Setter
    @Getter
    private Long shopId;

    /**
     * 模板名称
     */
    @Setter
    @Getter
    private String name;

    /**
     * 1 默认
     */
    @Setter
    @Getter
    private Byte defaultModel;

    /**
     * 模板类名
     */
    @Setter
    @Getter
    protected String templateBean;

    /**
     * 分包策略
     */
    @Setter
    @Getter
    protected String divideStrategy;

    @Getter
    @Setter
    protected DivideStrategy strategy;
    /**
     * 打包算法
     */
    @Setter
    @Getter
    protected String packAlgorithm;


    public TemplateType gotType() {
        assert this.templateBean != null;
        return TYPE.get(this.templateBean);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Template template = (Template) super.clone();
        template.setDefaultModel(COMMON);
        return template;
    }
}
