package cn.edu.xmu.oomall.service.controller.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 2023-dgn3-009
 *
 * @author huangzian
 */
@NoArgsConstructor
@Data

public class ExpressDto {

    @NotNull(message = "商铺渠道Id不能为空")
    private Long shopLogisticId;

    private String goodsType;

    private Long weight;

    private Integer payMethod;


    @Data
    @NoArgsConstructor
    public class ContactsInfo
    {
        @JsonProperty(value = "name")
        @NotBlank(message = "联系人姓名不能为空")
        private String name;
        @JsonProperty(value = "mobile")
        @NotBlank(message = "联系人电话不能为空")
        private String mobile;
        @JsonProperty(value = "regionId")
        @NotNull(message = "联系人的地区Id不能为空")
        private Long regionId;
        @JsonProperty(value = "address")
        @NotBlank(message = "联系人的地址不能为空")
        private String address;
    }
    @JsonProperty(value = "sender")
    @NotNull(message = "寄件人信息不能为空")
    private ContactsInfo sender;
    @JsonProperty(value = "delivery")
    @NotNull(message = "收件人信息不能为空")
    private ContactsInfo delivery;
}
