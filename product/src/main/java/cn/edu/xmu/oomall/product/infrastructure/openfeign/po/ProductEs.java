package cn.edu.xmu.oomall.product.infrastructure.openfeign.po;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEs implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long shopId;

    private String name;

    private String barcode;

}
