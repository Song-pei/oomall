package cn.edu.xmu.oomall.aftersale.controller.cancel;


import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.CustomerController;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(CustomerController.class)
public class NotFundOrStatusNotAllowedTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AftersaleOrderService aftersaleOrderService;

    /**
     * 测试售后单不存在场景
     */
    @Test
    public void testCustomerCancel_ResourceNotExist() throws Exception {
        // 模拟service层抛出售后单不存在异常
        Mockito.doThrow(new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在"))
                .when(aftersaleOrderService).customerCancel(anyLong(), any(UserToken.class));

        // 执行请求并验证结果
        mockMvc.perform(put("/aftersales/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()))
                .andExpect(jsonPath("$.errmsg").value(ReturnNo.RESOURCE_ID_NOTEXIST.getMessage()));
    }

    /**
     * 测试状态不允许取消场景
     */
    @Test
    public void testCustomerCancel_StateNotAllow() throws Exception {
        // 模拟service层抛出状态不允许异常
        Mockito.doThrow(new BusinessException(ReturnNo.STATENOTALLOW, "当前状态不允许取消"))
                .when(aftersaleOrderService).customerCancel(anyLong(), any(UserToken.class));

        // 执行请求并验证结果
        mockMvc.perform(put("/aftersales/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.STATENOTALLOW.getErrNo()))
                .andExpect(jsonPath("$.errmsg").value(ReturnNo.STATENOTALLOW.getMessage()));
    }

}
