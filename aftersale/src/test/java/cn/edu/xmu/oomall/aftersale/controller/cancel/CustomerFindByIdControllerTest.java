package cn.edu.xmu.oomall.aftersale.controller.cancel;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.CustomerController;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CustomerController 单元测试
 * 只测 Web 层，Service 层全部打桩
 */
@WebMvcTest(CustomerController.class)
class CustomerFindByIdControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    /* ========== 打桩 ========== */
    @MockitoBean
    private AftersaleOrderService aftersaleOrderService;

    /* ----------------------------------------------------------- */
    /* 正常场景：查到售后单并返回 VO                                */
    /* ----------------------------------------------------------- */
    @Test
    @DisplayName("顾客根据 id 查询售后单-成功")
    void customerSearch_ok() throws Exception {
        // 1. 构造 BO
        AftersaleOrder bo = new AftersaleOrder();
        bo.setId(1L);
        bo.setShopId(10L);
        bo.setCustomerId(100L);
        bo.setStatus(AftersaleOrder.UNAUDIT);
        bo.setType(0); // 换货

        // 2. 打桩 Service
        Mockito.when(aftersaleOrderService.customerSearch(eq(1L), any(UserToken.class)))
                .thenReturn(bo);

        // 3. 发起请求
        mvc.perform(get("/aftersales/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                // 4. 断言
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value(AftersaleOrder.UNAUDIT));
    }

    /* ----------------------------------------------------------- */
    /* 异常场景：售后单不存在                                       */
    /* ----------------------------------------------------------- */
    @Test
    @DisplayName("顾客根据 id 查询售后单-资源不存在")
    void customerSearch_notFound() throws Exception {
        // 1. 打桩 Service 抛业务异常
        Mockito.when(aftersaleOrderService.customerSearch(eq(999L), any(UserToken.class)))
                .thenThrow(new cn.edu.xmu.javaee.core.exception.BusinessException(
                        ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在"));

        // 2. 发起请求
        mvc.perform(get("/aftersales/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                // 3. 断言
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo()));
    }
}
