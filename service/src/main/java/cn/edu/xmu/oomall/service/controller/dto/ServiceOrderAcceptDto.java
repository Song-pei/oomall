package cn.edu.xmu.oomall.service.controller.dto;

import cn.edu.xmu.javaee.core.validation.NewGroup;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceOrderAcceptDto {
    @JsonProperty("confirm")
    @NotNull(message = "确认信息不能为空", groups = {NewGroup.class})
    private boolean confirm;

    @JsonProperty("maintainername")
    @NotBlank(message = "服务人员姓名不能为空", groups = {NewGroup.class})
    private String maintainername;


    @JsonProperty("maintainermobile")
    @NotBlank(message = "服务人员电话不能为空", groups = {NewGroup.class})
    private String maintainermobile;

    public boolean getConfirm(){return confirm;}
}
