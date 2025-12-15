package cn.edu.xmu.oomall.freight.adapter.controller.dto;

import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@CopyTo(Template.class)
@Data
public class TemplateDto {

    public static Integer WEIGHT = 0;
    public static Integer PIECE = 1;
    private String name;
    private Byte defaultModel;
    private Integer type;
    private String divideStrategy;
    private String packAlgorithm;

}
