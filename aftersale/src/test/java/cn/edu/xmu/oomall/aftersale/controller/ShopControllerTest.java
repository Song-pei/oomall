package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.controller.dto.OrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.feign.OrderClient;
import cn.edu.xmu.oomall.aftersale.service.feign.PaymentClient;
import cn.edu.xmu.oomall.aftersale.service.vo.PayTransVo;
import cn.edu.xmu.oomall.aftersale.service.vo.RefundTransVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Slf4j
@SpringBootTest(classes = AftersaleApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ShopControllerTest.MockConfig.class)
@Rollback(true)
public class ShopControllerTest {
    @TestConfiguration
    static class MockConfig {
        @Bean
        public RedisUtil redisUtil() {
            return Mockito.mock(RedisUtil.class);
        }
    }
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AftersaleOrderDao aftersaleOrderDao;
    @MockitoBean
    private ExpressClient expressClient;
    @MockitoBean
    private OrderClient orderClient;
    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    public void getAftersales() throws Exception {
        mockMvc.perform(get("/shops/1/aftersales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

//    @Test
//    public void auditAftersale() throws Exception {
//        String requestBody = "{\"confirm\": true, \"conclusion\": \"同意\", \"reason\": \"质量问题\"}";
//
//        mockMvc.perform(put("/shops/1/aftersales/1/confirm")
//                        .header("authorization", "Bearer test-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.errno").value(0))
//                .andDo(print());
//    }
//    @Test
//    public void auditAftersale_missingConfirm() throws Exception {
//        String requestBody = "{\"conclusion\": \"同意退款\", \"reason\": \"质量问题\"}";
//
//        mockMvc.perform(put("/shops/1/aftersales/1/confirm")
//                        .header("authorization", "Bearer test-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.errno").value(706)) // AFTERSALE_AUDIT_RESULT_EMPTY
//                .andDo(print());
//    }

    //测试验收售后单，售后单不存在
    @Test
    public void inspectAftersale_notExist() throws Exception {
        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/9999/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errno").value(4)) // RESOURCE_ID_NOTEXIST
                .andDo(print());
    }
    //测试验收售后单，状态不对
    @Test
    public void inspectAftersale_stateError() throws Exception {

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/1/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(7)) // AFTERSALE_STATE_NOT_ALLOW
                .andDo(print());
    }
    //测试验收售后单,店铺不匹配
    @Test
    public void inspectAftersale_shopNotMatch() throws Exception {
        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/999/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errno").value(17)) // SHOP_NOT_MATCH
                .andDo(print());
    }
    //测试验收售后单,不通过,成功
    @Test
    public void inspectAftersale_unpass() throws Exception {
        Long expectedExpressId = 10086L; // 预期的物流单 ID
        String expectedBillCode = "SF_TEST_SUCCESS";
        PackageResponseDTO mockResponse = new PackageResponseDTO(expectedExpressId, expectedBillCode);
        InternalReturnObject<PackageResponseDTO> successRet = new InternalReturnObject<>(mockResponse);

        Mockito.when(expressClient.createPackage(anyLong(), any(PackageCreateDTO.class), any()))
                .thenReturn(successRet);

        String requestBody = "{\"confirm\": false, \"exceptionDescription\": \"异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());

        // 4. 验证数据库状态更新
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(3L);

        // 验证基本状态
        assertEquals(AftersaleOrder.CANCEL.byteValue(), updatedPo.getStatus());
        assertEquals("异常", updatedPo.getExceptionDescription());

        // 验证 express_id 是否成功从 Feign 返回值保存到了数据库
        assertNotNull(updatedPo.getShopExpressId(), "shop_express_id 应该被保存到数据库中");
        assertEquals(expectedExpressId, updatedPo.getShopExpressId(), "数据库中的 express_id 应与 Mock 返回的一致");
    }
    //测试验收售后单,不通过,运单创建失败
    @Test
    public void inspectAftersale_unpass_expressCreateFail() throws Exception {
        // 模拟物流服务创建运单失败
        InternalReturnObject<PackageResponseDTO> failRet = new InternalReturnObject<>();
        failRet.setErrno(500);
        failRet.setErrmsg("物流服务异常");
        Mockito.when(expressClient.createPackage(anyLong(), any(PackageCreateDTO.class), any()))
                .thenReturn(failRet);

        String requestBody = "{\"confirm\": false, \"exceptionDescription\": \"异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/4/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(33)) // REMOTE_SERVICE_FAIL
                .andDo(print());
    }
    //测试验收售后单,换货通过
    @Test
    public void inspectAftersale_pass_exchange() throws Exception {
        Long expectedExpressId = 10086L; // 预期的物流单 ID
        String expectedBillCode = "SF_TEST_SUCCESS";
        PackageResponseDTO mockResponse = new PackageResponseDTO(expectedExpressId, expectedBillCode);
        InternalReturnObject<PackageResponseDTO> successRet = new InternalReturnObject<>(mockResponse);

        Mockito.when(expressClient.createPackage(anyLong(), any(PackageCreateDTO.class), any()))
                .thenReturn(successRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/4/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());

        // 4. 验证数据库状态更新
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(4L);
        // 验证基本状态
        assertEquals(AftersaleOrder.UNCHANGE.byteValue(), updatedPo.getStatus());
        // 验证 express_id 是否成功从 Feign 返回值保存到了数据库
        assertNotNull(updatedPo.getShopExpressId(), "shop_express_id 应该被保存到数据库中");
        assertEquals(expectedExpressId, updatedPo.getShopExpressId(), "数据库中的 express_id 应与 Mock 返回的一致");

    }
    //测试验收售后单,换货通过,运单创建失败
    @Test
    public void inspectAftersale_pass_exchange_expressCreateFail() throws Exception {
        // 模拟物流服务创建运单失败
        InternalReturnObject<PackageResponseDTO> failRet = new InternalReturnObject<>();
        failRet.setErrno(500);
        failRet.setErrmsg("物流服务异常");
        Mockito.when(expressClient.createPackage(anyLong(), any(PackageCreateDTO.class), any()))
                .thenReturn(failRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/4/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(33)) // REMOTE_SERVICE_FAIL
                .andDo(print());
    }
    //测试验收售后单,通过,退货,成功
    @Test
    public void inspectAftersale_pass_return() throws Exception {
        Long expectedExpressId = 5015L; // 预期的物流单 ID
        Long expectedPaymentId = 888L;
        OrderResponseDTO mockOrderResponse = new OrderResponseDTO(expectedExpressId, expectedPaymentId);
        InternalReturnObject<OrderResponseDTO> successOrderRet = new InternalReturnObject<>(mockOrderResponse);
        Mockito.when(orderClient.findOrderById(anyLong(), anyLong(), any()))
                .thenReturn(successOrderRet);

        PayTransVo mockPaymentResponse = new PayTransVo();
        mockPaymentResponse.setId(expectedPaymentId);
        mockPaymentResponse.setAmount(100L);
        mockPaymentResponse.setDivAmount(100L);
        InternalReturnObject<PayTransVo> successPaymentRet = new InternalReturnObject<>(mockPaymentResponse);
        Mockito.when(paymentClient.findPaymentById(anyLong(), anyLong(), any()))
                .thenReturn(successPaymentRet);

        RefundTransVo mockRefundResponse = new RefundTransVo();
        mockRefundResponse.setId(999L);
        InternalReturnObject<RefundTransVo> successRefundRet = new InternalReturnObject<>(mockRefundResponse);
        Mockito.when(paymentClient.createRefund(anyLong(), anyLong(), any(), any()))
                .thenReturn(successRefundRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());

        // 4. 验证数据库状态更新
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(3L);
        // 验证基本状态
        assertEquals(AftersaleOrder.UNREFUND.byteValue(), updatedPo.getStatus());
        assertEquals(999L, updatedPo.getRefundId());
    }
    //测试验收售后单,通过,退货,未找到订单
    @Test
    public void inspectAftersale_pass_return_orderNotFound() throws Exception {
        // 模拟订单服务未找到订单
        InternalReturnObject<OrderResponseDTO> failOrderRet = new InternalReturnObject<>();
        failOrderRet.setErrno(404);
        failOrderRet.setErrmsg("未找到订单");
        Mockito.when(orderClient.findOrderById(anyLong(), anyLong(), any()))
                .thenReturn(failOrderRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";
        mockMvc.perform(put("/shops/1/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(33)) // REMOTE_SERVICE_FAIL
                .andDo(print());
    }
    //测试验收售后单,通过,退货,未找到支付单
    @Test
    public void inspectAftersale_pass_return_paymentNotFound() throws Exception {
        Long expectedExpressId = 5015L; // 预期的物流单 ID
        Long expectedPaymentId = 888L;
        OrderResponseDTO mockOrderResponse = new OrderResponseDTO(expectedExpressId, expectedPaymentId);
        InternalReturnObject<OrderResponseDTO> successOrderRet = new InternalReturnObject<>(mockOrderResponse);
        Mockito.when(orderClient.findOrderById(anyLong(), anyLong(), any()))
                .thenReturn(successOrderRet);
        // 模拟支付服务调用失败
        InternalReturnObject<PayTransVo> failPaymentRet = new InternalReturnObject<>();
        failPaymentRet.setErrno(404);
        failPaymentRet.setErrmsg("未找到支付单");
        Mockito.when(paymentClient.findPaymentById(anyLong(), anyLong(), any()))
                .thenReturn(failPaymentRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";
        mockMvc.perform(put("/shops/1/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(33)) // REMOTE_SERVICE_FAIL
                .andDo(print());
    }
    //测试验收售后单,通过,退货,创建退款单失败
    @Test
    public void inspectAftersale_pass_return_refundCreateFail() throws Exception {
        Long expectedExpressId = 5015L; // 预期的物流单 ID
        Long expectedPaymentId = 888L;
        OrderResponseDTO mockOrderResponse = new OrderResponseDTO(expectedExpressId, expectedPaymentId);
        InternalReturnObject<OrderResponseDTO> successOrderRet = new InternalReturnObject<>(mockOrderResponse);
        Mockito.when(orderClient.findOrderById(anyLong(), anyLong(), any()))
                .thenReturn(successOrderRet);

        PayTransVo mockPaymentResponse = new PayTransVo();
        mockPaymentResponse.setId(expectedPaymentId);
        mockPaymentResponse.setAmount(100L);
        mockPaymentResponse.setDivAmount(100L);
        InternalReturnObject<PayTransVo> successPaymentRet = new InternalReturnObject<>(mockPaymentResponse);
        Mockito.when(paymentClient.findPaymentById(anyLong(), anyLong(), any()))
                .thenReturn(successPaymentRet);
        // 模拟退款单创建失败
        InternalReturnObject<RefundTransVo> failRefundRet = new InternalReturnObject<>();
        failRefundRet.setErrno(500);
        failRefundRet.setErrmsg("退款单创建失败");
        Mockito.when(paymentClient.createRefund(anyLong(), anyLong(), any(), any()))
                .thenReturn(failRefundRet);

        String requestBody = "{\"confirm\": true, \"exceptionDescription\": \"无异常\"}";

        mockMvc.perform(put("/shops/1/aftersales/3/receive")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(33)) // REMOTE_SERVICE_FAIL
                .andDo(print());
    }




    // ==========================================
    //根据 ID 查询售后单详情
    // Path: GET /shops/{shopId}/aftersales/{id}
    // ==========================================

    /**
     * 测试：成功查询售后单详情
     * 前置条件：数据库中存在 ID=1 的售后单，且属于 Shop=1
     */
    @Test
    public void getAftersaleById_Success() throws Exception {
        // 假设 ID 1 是存在的且属于 Shop 1 (根据你现有的测试数据推断)
        mockMvc.perform(get("/shops/1/aftersales/1")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0)) // 成功
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.shopId").value(1))
                .andDo(print());
    }

    /**
     * 测试：查询不存在的售后单 ID
     * 预期：返回 404 状态码，errno = 4 (RESOURCE_ID_NOTEXIST)
     */
    @Test
    public void getAftersaleById_NotFound() throws Exception {
        mockMvc.perform(get("/shops/1/aftersales/999999") // 使用一个不存在的ID
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Http 404
                .andExpect(jsonPath("$.errno").value(4)) // ReturnNo.RESOURCE_ID_NOTEXIST
                .andDo(print());
    }

    /**
     * 测试：URL中的 ShopId 与 售后单实际所属的 ShopId 不一致
     * 场景：查询 ID=1 的售后单（属于Shop 1），但是 URL 请求的是 Shop 2
     * 预期：为了安全，应该报“资源不存在”或者“无权访问”
     * 根据上一轮代码逻辑：if (!po.getShopId().equals(shopId)) throw RESOURCE_ID_NOTEXIST
     */
    @Test
    public void getAftersaleById_ShopMismatch() throws Exception {
        // 这里的 ID 1 存在，但它属于 Shop 1，我们试图通过 Shop 2 的路径去访问它
        mockMvc.perform(get("/shops/2/aftersales/1")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 遵循上一轮代码逻辑，抛出的是 RESOURCE_ID_NOTEXIST
                .andExpect(jsonPath("$.errno").value(4)) // ReturnNo.RESOURCE_ID_NOTEXIST ("该店铺下无此售后单")
                .andDo(print());
    }

}