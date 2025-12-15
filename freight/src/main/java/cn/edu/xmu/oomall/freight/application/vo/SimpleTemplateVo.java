//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.freight.application.vo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.oomall.freight.domain.bo.template.Template;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@CopyFrom({Template.class})
@Getter
public class SimpleTemplateVo {
    @Setter
    private Long id;
    @Setter
    private String name;
    @JsonProperty(value = "default")
    private Boolean defaultModel;
    public void setDefaultModel(Byte defaultModel) {
        this.defaultModel = defaultModel == 0;
    }
}
