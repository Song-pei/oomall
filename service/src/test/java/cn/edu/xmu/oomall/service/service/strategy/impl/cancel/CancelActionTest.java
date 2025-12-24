package cn.edu.xmu.oomall.service.service.strategy.impl.cancel;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServiceApplication.class)
@AutoConfigureMockMvc
@Transactional
@DisplayName("服务单取消接口综合测试")
class CancelActionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private ExpressClient expressClient;

    private final Long SHOP_ID = 200L;
    private final Long MAINTAINER_ID = 1L;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("DELETE FROM service_service WHERE id IN (500,600,700,800,801)");
        jdbcTemplate.execute("DELETE FROM service_provider WHERE id = " + MAINTAINER_ID);
    }

    /** 场景1：上门/待接受(UNACCEPT=0) 简单取消，期望转为 CANCEL=4 */
    @Test
    void simpleCancel_success() throws Exception {
        Long orderId = 700L;
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + orderId + ", " + SHOP_ID + ", 0, 0, 350206, '测试地址', '小张', '13955556666', NOW())");

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));

        entityManager.flush();
        entityManager.clear();
        Integer statusVal = jdbcTemplate.queryForObject(
                "SELECT status FROM service_service WHERE id = ?", Integer.class, orderId);
        org.junit.jupiter.api.Assertions.assertEquals(4, statusVal);
    }

    /** 场景2：寄件维修(type=1, REPAIRING=3) 取消成功，Feign createPackage 成功 */
    @Test
    void expressCancel_success() throws Exception {
        Long orderId = 500L;
        // 服务商信息（寄件取消需要寄件地址）
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (" + MAINTAINER_ID + ", '维修中心', '王经理', '厦门市软件园', '13811112222', 350203, 1, NOW())");

        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + orderId + ", " + MAINTAINER_ID + ", " + SHOP_ID +
                ", 350206, '用户家庭地址', '小张', '13955556666', 3, 1, NOW())");

        ExpressPo mockExpress = new ExpressPo();
        mockExpress.setId(999L);
        Mockito.when(expressClient.createPackage(eq(SHOP_ID), any(), any(), any()))
                .thenReturn(new InternalReturnObject<>(mockExpress));

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));
    }

    /** 场景3：寄件维修(type=1, REPAIRING=3) 取消失败，Feign 返回 REMOTE_SERVICE_FAIL */
    @Test
    void expressCancel_remoteFail() throws Exception {
        Long orderId = 800L;
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (" + MAINTAINER_ID + ", '维修中心', '王经理', '厦门市软件园', '13811112222', 350203, 1, NOW())");

        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + orderId + ", " + MAINTAINER_ID + ", " + SHOP_ID +
                ", 350206, '用户家庭地址', '小张', '13955556666', 3, 1, NOW())");

        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "物流模块忙"));

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                // 当前控制器把 BusinessException 兜底成 INTERNAL_SERVER_ERR
                .andExpect(jsonPath("$.errno", is(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo())));
    }

    /** 场景4：寄件待收件(type=1, UNCHECK=2) 拦截运单成功，cancelPackage 成功 */
    @Test
    void interceptCancel_success() throws Exception {
        Long orderId = 600L;
        Long expressId = 8888L;

        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, express_id, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + orderId + ", " + SHOP_ID + ", 2, 1, " + expressId +
                ", 350206, '用户地址', '小张', '13955556666', NOW())");

        Mockito.when(expressClient.cancelPackage(eq(SHOP_ID), eq(expressId), any(), any()))
                .thenReturn(new InternalReturnObject<>(new ExpressPo()));

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));
    }

    /** 场景5：寄件待收件拦截失败，cancelPackage 返回 REMOTE_SERVICE_FAIL */
    @Test
    void interceptCancel_remoteFail() throws Exception {
        Long orderId = 801L;
        Long expressId = 8889L;

        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, express_id, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + orderId + ", " + SHOP_ID + ", 2, 1, " + expressId +
                ", 350206, '用户地址', '小张', '13955556666', NOW())");

        Mockito.when(expressClient.cancelPackage(any(), any(), any(), any()))
                .thenReturn(new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "运单已签收，无法拦截"));

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                // 当前控制器兜底为 INTERNAL_SERVER_ERR
                .andExpect(jsonPath("$.errno", is(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo())));
    }

    /** 场景6：状态不允许 - 已完成 FINISH=5 */
    @Test
    void cancel_StateNotAllow_WhenFinished() throws Exception {
        Long orderId = 901L;
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + orderId + ", " + SHOP_ID + ", 5, 0, 350206, '已完成地址', '用户B', '13900000001', NOW())");

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno", is(ReturnNo.STATENOTALLOW.getErrNo())));
    }

    /** 场景7：未找到取消策略 (type 未配置) */
    @Test
    void cancel_NoStrategyFound() throws Exception {
        Long orderId = 902L;
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, shop_id, status, type, region_id, address, consignee, mobile, gmt_create) " +
                "VALUES (" + orderId + ", " + SHOP_ID + ", 0, 9, 350206, '未配置策略地址', '用户C', '13900000002', NOW())");

        mockMvc.perform(post("/maintainers/{did}/services/{id}/cancel", MAINTAINER_ID, orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errno", is(ReturnNo.FIELD_NOTVALID.getErrNo())));
    }
}