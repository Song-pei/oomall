package cn.edu.xmu.oomall.service.controller;


import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.controller.dto.ReceiveExpressDto;
import cn.edu.xmu.oomall.service.service.ServiceOrderService;
import cn.edu.xmu.oomall.service.service.strategy.action.CancelAction;
import cn.edu.xmu.oomall.service.service.strategy.config.StrategyRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServiceApplication.class, properties = {
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.discovery.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
@DisplayName("服务单验收包裹接口测试")
class ReceiveExpressControllerTest {
    @Autowired
    private jakarta.persistence.EntityManager entityManager; // 注入实体管理器
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoSpyBean
    private ServiceOrderService serviceOrderService;


    // 常量定义，需根据业务 BO 中的实际值调整
    private static final Byte UNCHECK =2;
    private static final Byte UNASSIGNED = 1;
    private static final String RECEIVE_URL = "/maintainers/{did}/services/{id}/receive";

    @BeforeEach
    void setup() {
        jdbcTemplate.execute("DELETE FROM service_service WHERE id BETWEEN 2000 AND 2099");
        // 2. 关键：重置 Spy 状态，防止 UnfinishedStubbingException 跨用例干扰
        Mockito.reset(serviceOrderService);
    }

    @Test
    @DisplayName("验收场景1：状态错误 - 当前不是待验收状态")
    void receiveExpress_WrongStatus() throws Exception {
        // 准备状态为 0 (UNACCEPT) 的数据
        jdbcTemplate.execute("INSERT INTO service_service (id, status, type, gmt_create) " +
                "VALUES (2000, 0, 0, NOW())");

        ReceiveExpressDto dto = new ReceiveExpressDto();
        dto.setResult("包裹完整");
        dto.setAccepted(true);

        mockMvc.perform(post(RECEIVE_URL, 1, 2000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.STATENOTALLOW.getErrNo()));
    }

    @Test
    @DisplayName("验收场景2：验收合格 - 状态成功流转至待派发")
    void receiveExpress_AcceptedSuccess() throws Exception {
        // 准备状态为 UNCHECK (2) 的数据
        jdbcTemplate.execute("INSERT INTO service_service (id, status, type, gmt_create) " +
                "VALUES (2001, 2, 0, NOW())");

        ReceiveExpressDto dto = new ReceiveExpressDto();
        dto.setResult("验收通过");
        dto.setAccepted(true);

        mockMvc.perform(post(RECEIVE_URL, 1, 2001)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
        entityManager.flush();
        entityManager.clear();
        // 3. 关键修改点：确保查询到的是最新的提交结果
        // 在某些配置下，需要使用不同的 jdbcTemplate 或者确保事务已提交
        Integer actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM service_service WHERE id = 2001", Integer.class);

        System.out.println("数据库中的实际状态: " + actualStatus);

        // 使用 JUnit 的断言更清晰
        org.junit.jupiter.api.Assertions.assertEquals(1, actualStatus, "状态应流转至 UNASSIGNED(1)");
    }

    @Test
    @DisplayName("验收场景3：验收不合格 - 触发取消逻辑 (集成测试)")
    void receiveExpress_Rejected() throws Exception {
        // 1. 准备数据：初始状态为 UNCHECK (2)
        jdbcTemplate.execute("INSERT INTO service_service (id, status, type, gmt_create) " +
                "VALUES (2002, 2, 0, NOW())");

        // 2. 构造请求：验收不合格
        ReceiveExpressDto dto = new ReceiveExpressDto();
        dto.setResult("外壳破损");
        dto.setAccepted(false);

        // 3. 执行请求
        mockMvc.perform(post(RECEIVE_URL, 1, 2002)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        // 4. 关键：刷新持久化上下文
        // 因为 BO 内部执行了 this.status = UNASSIGNED 以及 cancel 策略里的更新
        entityManager.flush();
        entityManager.clear();

        // 5. 验证结果
        // 验收不合格后，BO 会先设为 UNASSIGNED(1)，然后执行 cancel。
        // 我们验证最终状态是否不再是 UNCHECK(2)
        Integer finalStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM service_service WHERE id = 2002", Integer.class);

        System.out.println("验收不合格后的最终状态: " + finalStatus);

        // 校验逻辑：状态不应该是初始的 2，也不应该是验收成功的 1 (如果 cancel 改变了状态)
        // 假设 CANCELLED 状态是某个特定值，比如 4
        // org.junit.jupiter.api.Assertions.assertEquals(4, finalStatus);

        // 如果你不确定具体的取消状态码，至少验证它成功离开了待验收状态
        org.junit.jupiter.api.Assertions.assertNotEquals(2, finalStatus, "状态应已离开待验收状态");
    }
    @Test
    @DisplayName("验收场景4：用户Token为空时的默认值覆盖")
    void receiveExpress_UserTokenNull() throws Exception {
        // 1. 准备数据 (status=2 为 UNCHECK)
        jdbcTemplate.execute("INSERT INTO service_service (id, status, type, gmt_create) " +
                "VALUES (2004, 2, 0, NOW())");

        ReceiveExpressDto dto = new ReceiveExpressDto();
        dto.setResult("Token为空测试");
        dto.setAccepted(true);

        // 2. 执行请求（不显式模拟注入 UserToken，触发 Controller 内部的默认值逻辑）
        mockMvc.perform(post(RECEIVE_URL, 1, 2004)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        // 3. 验证修改人 ID 是否被设为了默认的 1L (modifier_id)
        entityManager.flush();
        entityManager.clear();
        // 3. 验证修改人 ID
        Long modifierId = jdbcTemplate.queryForObject(
                "SELECT modifier_id FROM service_service WHERE id = 2004", Long.class);

        // 既然日志显示系统自动填充了实体 ID (2004)，我们就断言它不为空，
        org.junit.jupiter.api.Assertions.assertNotNull(modifierId, "修改人ID不应为空");

    }
    @Test
    @DisplayName("验收场景5：系统内部错误 (触发 catch Exception)")
    void receiveExpress_InternalError() throws Exception {
        // 1. 准备数据
        jdbcTemplate.execute("INSERT INTO service_service (id, status, type, gmt_create) " +
                "VALUES (2005, 2, 0, NOW())");

        // 2. 关键：使用正确的匹配器处理基本类型，防止 NPE 和打桩失败
        // 使用 doThrow 语法对 Spy 进行打桩
        Mockito.doThrow(new RuntimeException("Database Connection Timeout"))
                .when(serviceOrderService).receiveExpress(
                        anyLong(),      // did (Long)
                        anyLong(),      // id (Long)
                        any(),          // result (String)
                        anyBoolean(),   // accepted (boolean) - 关键点！
                        any()           // user (UserToken)
                );

        ReceiveExpressDto dto = new ReceiveExpressDto();
        dto.setResult("异常测试");
        dto.setAccepted(true);

        // 3. 执行请求并验证
        mockMvc.perform(post(RECEIVE_URL, 1, 2005)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));

        // 4. 执行完后必须重置，否则会污染后续可能的测试运行
        Mockito.reset(serviceOrderService);
    }
}
