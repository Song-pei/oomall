package cn.edu.xmu.oomall.service.controller.dto;

import cn.edu.xmu.javaee.core.validation.NewGroup;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsigneeDto {

    @JsonProperty("name")
    @NotBlank(message = "联系人姓名不能为空", groups = {NewGroup.class})
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @JsonProperty("mobile")
    @NotBlank(message = "联系人电话不能为空", groups = {NewGroup.class})
    private String mobile;

    @JsonProperty("regionId")
    @NotNull(message = "联系人的地区Id不能为空", groups = {NewGroup.class})
    private Long regionId;

    @JsonProperty("address")
    @NotBlank(message = "联系人的地址不能为空", groups = {NewGroup.class})
    private String address;
}