package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;


import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.JacksonUtil;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import org.junit.jupiter.api.Assertions;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServiceApplication.class)
@AutoConfigureMockMvc
@Transactional // 测试完成后自动回滚，保持数据库干净
public class SimpleCancelActionTest {
    @Autowired
    private jakarta.persistence.EntityManager entityManager; // 注入实体管理器
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Long TEST_ORDER_ID = 700L;
    private final Long TEST_SHOP_ID = 200L;
    @MockitoBean
    private ExpressClient expressClient;
    @BeforeEach
    public void setup() {
        // 清理并准备一条“待审核”状态的服务单 (status = 0)
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + TEST_ORDER_ID);
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + TEST_ORDER_ID + ", " + TEST_SHOP_ID + ", 0, 0, 350206, '测试地址', '小张', '13955556666', NOW())");

        System.out.println("---- 数据准备完毕：待审核服务单，准备执行简单取消 ----");
    }

    @Test
    public void testSimpleCancel_Success() throws Exception {
        // 执行取消操作
        // 这里的 URL 路径请根据你的 Controller 实际配置修改，例如 /maintainers/{shopId}/services/{id}/cancel
        mockMvc.perform(post("/maintainers/{shopId}/services/{id}/cancel", TEST_SHOP_ID, TEST_ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
        // 关键点：强制将内存中的修改同步到数据库
        entityManager.flush();
        entityManager.clear(); // 清除缓存，确保接下来的查询穿透到数据库
        // 验证数据库状态是否已变为“已取消” (假设 ServiceOrder.CANCEL 对应的数据库值为 4)
        // 请根据你 ServiceOrder 类中定义的 CANCEL 常量实际值修改下面的数字
        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM service_service WHERE id = ?", Integer.class, TEST_ORDER_ID);

        Assertions.assertEquals(4, status, "数据库状态应更新为 4 (已取消)");
        System.out.println("---- 测试通过：服务单状态已成功修改为取消状态 ----");
    }
}