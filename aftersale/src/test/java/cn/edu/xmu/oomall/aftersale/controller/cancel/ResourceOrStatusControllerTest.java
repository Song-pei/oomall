package cn.edu.xmu.oomall.aftersale.controller.cancel;


import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.CustomerController;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(CustomerController.class)
public class ResourceOrStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AftersaleOrderService aftersaleOrderService;
    @MockitoBean
    private AftersaleOrderDao aftersaleOrderDao;



    /**
     * 测试 service层抛出异常后controller的行为
     */
    @Test
    public void testCustomerCancel_ResourceNotExist2() throws Exception {

        // 模拟service层抛出售后单不存在异常
        Mockito.doThrow(new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在"))
                .when(aftersaleOrderService).customerCancel(anyLong(), any(UserToken.class));

        // 执行请求并验证结果
        mockMvc.perform(put("/aftersales/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));
        when(aftersaleOrderDao.findById(anyLong())).thenReturn(null);


    }

    /**
     * 测试状态 service抛出状态异常，controller行为
     */
    @Test
    public void testCustomerCancel_StateNotAllow2() throws Exception {
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
