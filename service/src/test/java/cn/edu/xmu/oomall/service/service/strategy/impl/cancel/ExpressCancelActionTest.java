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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExpressCancelActionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ExpressClient expressClient;

    private final Long TEST_ORDER_ID = 500L;
    private final Long TEST_MAINTAINER_ID = 1L;
    private final Long TEST_SHOP_ID = 200L;

    @BeforeEach
    public void setup() {
        // 1. 清理数据
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + TEST_ORDER_ID);
        jdbcTemplate.execute("DELETE FROM service_provider WHERE id = " + TEST_MAINTAINER_ID);

        // 2. 插入服务商数据 (Action 执行时需要查找 ServiceProvider 获取寄件地址)
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (" + TEST_MAINTAINER_ID + ", '维修中心', '王经理', '厦门市软件园', '13811112222', 350203, 1, NOW())");

        // 3. 插入服务单数据
        // 条件：type=1 (寄件), status=3 (维修中) -> 匹配策略规则中的 expressCancelAction
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + TEST_ORDER_ID + ", " + TEST_MAINTAINER_ID + ", " + TEST_SHOP_ID +
                ", 350206, '用户家庭地址', '小张', '13955556666', 3, 1, NOW())");

        System.out.println("---- 数据准备完毕：寄件型(type=1)维修中(status=3)服务单 ----");
    }

    /**
     * 测试成功取消：远程调用物流成功
     */
    @Test
    public void testCancelServiceOrder_Success() throws Exception {
        // 模拟物流模块返回成功的 ExpressPo
        ExpressPo mockExpress = new ExpressPo();
        mockExpress.setId(999L);
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(mockExpress);

        // 打桩：模拟创建寄回运单
        Mockito.when(expressClient.createPackage(eq(TEST_SHOP_ID), any(), any(), any()))
                .thenReturn(mockRet);

        
        // 假设路径是 /maintainers/{did}/services/{id}/cancel
        mockMvc.perform(post("/maintainers/1/services/" + TEST_ORDER_ID + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        System.out.println("---- ExpressCancelAction 成功执行，已模拟创建寄回运单 ----");
    }

    /**
     * 测试取消失败：物流模块返回错误码
     */
    @Test
    public void testCancelServiceOrder_RemoteFail() throws Exception {
        // 模拟物流模块返回失败
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "物流模块忙");

        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        mockMvc.perform(post("/maintainers/1/services/" + TEST_ORDER_ID + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                // Action 抛出 BusinessException(REMOTE_SERVICE_FAIL)，Controller 捕获并返回 INTERNAL_SERVER_ERR (根据你提供的 Controller 代码)
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }
}