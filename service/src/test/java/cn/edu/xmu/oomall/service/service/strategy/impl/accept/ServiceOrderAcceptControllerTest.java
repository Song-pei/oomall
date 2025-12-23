package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderAcceptDto;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
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
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        "ser-vice.strategies[1].bean-name=expressAcceptAction"
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
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()));}
}
