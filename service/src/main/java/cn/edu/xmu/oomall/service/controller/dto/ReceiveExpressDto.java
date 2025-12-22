package cn.edu.xmu.oomall.service.controller.dto;


import cn.edu.xmu.javaee.core.validation.NewGroup;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
public class ReceiveExpressDto {
    @JsonProperty("accepted")
    @NotNull(message = "寄回商品合格信息不能为空", groups = {NewGroup.class})
    private boolean accepted;

    @JsonProperty("result")
    @NotBlank(message = "处理结果不能为空", groups = {NewGroup.class})
    private String result;
    public boolean getAccepted(){return accepted;}

}
