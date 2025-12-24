package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.JwtHelper;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderAcceptDto;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SimpleAcceptAction 集成测试
 * 验证 type=0 (上门服务) 时，API 是否能正确路由并执行
 */
@SpringBootTest(classes = ServiceApplication.class)
@AutoConfigureMockMvc
@Transactional // 测试完成后自动回滚数据库修改
class SimpleAcceptActionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 必须 Mock 掉 ExpressClient，
     * 因为 StrategyRouter 初始化时会去 context 找 expressCancelAction，
     * 而 expressCancelAction 依赖了 ExpressClient。
     */
    @MockitoBean
    private ExpressClient expressClient;

    @BeforeEach
    public void setup() {
        // 1. 清理数据 (使用 ID 300 避免干扰)
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = 300");
        jdbcTemplate.execute("DELETE FROM service_provider WHERE id = 1");

        // 2. 插入服务商数据
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (1, '快捷服务商', '服务商联系人', '厦门市思明区', '13800000000', 350203, 1, NOW())");

        // 3. 插入服务单数据：注意 type=0 (上门服务)
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (300, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 0, NOW())");

        System.out.println("---- Database Setup Complete for SimpleAcceptAction (Type 0) ----");
    }

    @Test
    public void testSimpleAccept_Success() throws Exception {
        // 准备 DTO
        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("上门员工");
        dto.setMaintainermobile("13899998888");

        // 执行请求
        // 注意路径使用参考类中的 /maintainers/{did}/services/{id}/accept
        mockMvc.perform(post("/maintainers/1/services/300/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        System.out.println("---- SimpleAcceptAction (Type 0) 测试成功 ----");
    }
}