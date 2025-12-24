package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.controller.dto.ReceiveExpressDto;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderAcceptDto;
import cn.edu.xmu.oomall.service.service.ServiceOrderService;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import cn.edu.xmu.oomall.service.service.strategy.config.StrategyRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Qualifier;
@SpringBootTest(classes = ServiceApplication.class, properties = {
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.discovery.enabled=false",
        // 注入测试所需的策略路由配置
        "ser-vice.strategies[0].type=0",
        "ser-vice.strategies[0].status=0",
        "ser-vice.strategies[0].opt=ACCEPT",
        "ser-vice.strategies[0].bean-name=simpleAcceptAction",
        "ser-vice.strategies[1].type=1",
        "ser-vice.strategies[1].status=0",
        "ser-vice.strategies[1].opt=ACCEPT",
        "ser-vice.strategies[1].bean-name=expressAcceptAction",

})
@AutoConfigureMockMvc
@Transactional

@DisplayName("服务单接受接口测试")
class ServiceOrderAcceptControllerTest {


    @MockitoBean
    private ExpressClient expressClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    private ServiceOrderService serviceOrderService;

    private static final String ACCEPT_URL = "/maintainers/{did}/services/{id}/accept";

    @BeforeEach
    public void setup() {
        // 清理旧数据，确保测试环境干净
        jdbcTemplate.execute("DELETE FROM service_service WHERE id IN (100, 300)");
        jdbcTemplate.execute("DELETE FROM service_provider WHERE id = 1");

        // 插入公共服务商数据
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (1, '快捷服务商', '服务商联系人', '厦门市思明区', '13800000000', 350203, 1, NOW())");
    }

    @Test
    @DisplayName("场景1：上门服务接受成功 (Type=0)")
    void acceptSimple_Success() throws Exception {
        // 准备 Type=0 的数据
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (300, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 0, NOW())");

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("上门员工");
        dto.setMaintainermobile("13899998888");

        mockMvc.perform(post(ACCEPT_URL, 1, 300)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }

    @Test
    @DisplayName("场景2：寄件服务接受成功 (Type=1) - 涉及Feign调用")
    void acceptExpress_Success() throws Exception {
        // 准备 Type=1 的数据
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (100, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 1, NOW())");

        // Mock 外部物流接口返回
        ExpressPo mockExpressPo = new ExpressPo();
        mockExpressPo.setId(888L);
        mockExpressPo.setBillCode("TestBillCode");
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(mockExpressPo);

        Mockito.reset(expressClient);
        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");

        mockMvc.perform(post(ACCEPT_URL, 1, 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }

    @Test
    @DisplayName("场景3：寄件服务接受失败 - 远程物流模块异常")
    void acceptExpress_RemoteFail() throws Exception {
        // 准备 Type=1 的数据
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (100, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 1, NOW())");

        // 模拟远程调用返回错误码
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "物流模块异常");

        Mockito.reset(expressClient);
        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");

        mockMvc.perform(post(ACCEPT_URL, 1, 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()));
    }

    @Test
    @DisplayName("场景4：寄件服务接受失败 - 远程返回成功但数据为空")
    void acceptExpress_DataNull() throws Exception {
        // 1. 确保数据与策略路由配置完全一致 (Type=1, Status=0)
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = 101"); // 预防性清理
        jdbcTemplate.execute("INSERT INTO service_service (id, maintainer_id, shop_id, status, type, gmt_create) " +
                "VALUES (101, 1, 200, 0, 1, NOW())");

        // 2. 构造 Mock 返回 (errno=0, data=null)
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>();
        mockRet.setErrno(0);
        mockRet.setData(null);
        mockRet.setErrmsg("No data returned");

        ExpressClient mock = AopTestUtils.getUltimateTargetObject(expressClient);
        Mockito.reset(mock);
        Mockito.when(mock.createPackage(any(), any(), any(), any())).thenReturn(mockRet);

        // 3. 执行请求
        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");

        mockMvc.perform(post(ACCEPT_URL, 1, 101)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // 4. 此时应该通过路由，进入 Action 抛出 33 (REMOTE_SERVICE_FAIL)
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()));
    }

    @Test
    @DisplayName("场景5：接受失败 - 当前状态不允许接受 (status != UNACCEPT)")
    void accept_StateNotAllow() throws Exception {
        // 准备一个状态不为 0 (假设 0 是 UNACCEPT) 的数据，例如 status = 1
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, status, type, gmt_create) " +
                "VALUES (400, 1, 200, 1, 0, NOW())");

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        mockMvc.perform(post(ACCEPT_URL, 1, 400)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()) // 业务异常通常返回 200 或相应错误码
                .andExpect(jsonPath("$.errno").value(ReturnNo.STATENOTALLOW.getErrNo()));
    }

    @Test
    @DisplayName("场景6：接受失败 - 未找到对应策略 (Route returns null)")
    void accept_NoStrategyFound() throws Exception {
        // 准备一个在 @SpringBootTest 属性配置中没有定义过的 type
        // 配置中只有 type=0 和 type=1，这里使用 type=9
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, status, type, gmt_create) " +
                "VALUES (500, 1, 200, 0, 9, NOW())");

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        mockMvc.perform(post(ACCEPT_URL, 1, 500)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.errno").value(ReturnNo.FIELD_NOTVALID.getErrNo()));
    }
    @Test
    @DisplayName("场景7：服务单拒绝成功 (confirm = false)")
    void acceptServiceOrder_Reject() throws Exception {
        // 不需要准备数据库数据，因为 confirm 为 false 时不进入 service 层
        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(false); // 设置为拒绝
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        mockMvc.perform(post(ACCEPT_URL, 1, 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }

    @Test
    @DisplayName("场景8：用户Token为空时的默认值覆盖")
    void acceptServiceOrder_UserTokenNull() throws Exception {
        // 准备数据
        jdbcTemplate.execute("INSERT INTO service_service (id, maintainer_id, shop_id, status, type, gmt_create) " +
                "VALUES (700, 1, 200, 0, 0, NOW())");

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        // 在 mockMvc 请求时不传入 UserToken（或者确保拦截器没把 User 塞进去）
        // 此时 Controller 内部会执行 user = new UserToken(); user.setId(1L);
        mockMvc.perform(post(ACCEPT_URL, 1, 700)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }

    @Test
    @DisplayName("验收场景9：系统内部错误 (触发 catch Exception)")
    void acceptServiceOrde_InternalError() throws Exception {
        // 准备 Type=1 的数据
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (100, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 1, NOW())");

        // 2. 关键：使用正确的匹配器处理基本类型，防止 NPE 和打桩失败
        // 使用 doThrow 语法对 Spy 进行打桩
        Mockito.doThrow(new RuntimeException("Database Connection Timeout"))
                .when(serviceOrderService).acceptServiceOrder(
                        anyLong(),      // did (Long)
                        anyLong(),      // id (Long)
                        any()           // user (UserToken)
                );

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        // 3. 执行请求并验证
        mockMvc.perform(post(ACCEPT_URL, 1, 2005)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));

        // 4. 执行完后必须重置，否则会污染后续可能的测试运行
        Mockito.reset(serviceOrderService);
    }
}
