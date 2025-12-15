//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.product.infrastructure.openfeign.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运费模板
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class Template {
    Long id;
    String name;
}
