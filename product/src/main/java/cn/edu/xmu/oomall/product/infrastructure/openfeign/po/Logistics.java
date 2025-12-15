package cn.edu.xmu.oomall.product.infrastructure.openfeign.po;
import lombok.*;
/**
 *
 * @author jyx
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Logistics {

    @Getter
    private Long id;
    private String name;

}
