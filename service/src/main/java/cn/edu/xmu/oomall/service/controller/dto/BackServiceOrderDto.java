package cn.edu.xmu.oomall.service.controller.dto;

import cn.edu.xmu.javaee.core.validation.NewGroup;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 维修师傅退回服务单请求体
 */
@Data
public class BackServiceOrderDto {

    @JsonProperty("result")
    @NotBlank(message = "退回说明不能为空", groups = {NewGroup.class})
    private String result;
}