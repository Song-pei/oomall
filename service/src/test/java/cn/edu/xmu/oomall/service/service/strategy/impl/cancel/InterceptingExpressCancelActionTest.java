package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InterceptingExpressCancelActionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ExpressClient expressClient;

    private final Long TEST_ORDER_ID = 600L;
    private final Long TEST_EXPRESS_ID = 8888L;
    private final Long TEST_SHOP_ID = 200L;

    @BeforeEach
    public void setup() {
        // 1. 清理历史数据
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + TEST_ORDER_ID);

        // 2. 插入服务单数据
        // 条件：type=1 (寄件), status=1 (待收件/待派工)
        // 必须设置 express_id，因为拦截逻辑需要它
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, express_id, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + TEST_ORDER_ID + ", " + TEST_SHOP_ID + ", 2, 1, " + TEST_EXPRESS_ID +
                ", 350206, '用户地址', '小张', '13955556666', NOW())");

        System.out.println("---- 数据准备完毕：寄件型待收件服务单，准备拦截运单 " + TEST_EXPRESS_ID + " ----");
    }

    /**
     * 测试成功拦截物流
     */
    @Test
    public void testInterceptCancel_Success() throws Exception {
        // 1. 模拟物流模块返回
        ExpressPo mockExpress = new ExpressPo();
        mockExpress.setId(TEST_EXPRESS_ID);
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(mockExpress);

        // 2. 打桩 cancelPackage 方法
        // 注意：Action 传入的是 shopId, expressId, user, userLevel
        Mockito.when(expressClient.cancelPackage(eq(TEST_SHOP_ID), eq(TEST_EXPRESS_ID), any(), any()))
                .thenReturn(mockRet);

        // 3. 执行请求
        mockMvc.perform(post("/maintainers/1/services/" + TEST_ORDER_ID + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        System.out.println("---- InterceptingCancelAction 测试通过，物流成功拦截 ----");
    }

    /**
     * 测试物流拦截失败（远程服务返回异常）
     */
    @Test
    public void testInterceptCancel_RemoteFail() throws Exception {
        // 1. 模拟物流拦截返回错误
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "运单已签收，无法拦截");

        Mockito.when(expressClient.createPackage(any(), any(), any(), any())) // 之前的打桩
                .thenReturn(mockRet);
        Mockito.when(expressClient.cancelPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        // 2. 执行请求
        mockMvc.perform(post("/maintainers/1/services/" + TEST_ORDER_ID + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                // Action 抛出 BusinessException，Controller 捕获后返回 INTERNAL_SERVER_ERR (errno: 2)
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }
}