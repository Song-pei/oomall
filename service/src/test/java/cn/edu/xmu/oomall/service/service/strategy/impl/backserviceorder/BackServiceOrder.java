package cn.edu.xmu.oomall.service.service.strategy.impl.backserviceorder;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServiceApplication.class)
@AutoConfigureMockMvc
@Transactional
public class BackServiceOrder {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private final Long TEST_ORDER_ID = 800L;
    private final Long TEST_MAINTAINER_ID = 1L;
    private final Long TEST_SHOP_ID = 200L;

    @BeforeEach
    public void setup() {
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + TEST_ORDER_ID);
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + TEST_ORDER_ID + ", " + TEST_MAINTAINER_ID + ", " + TEST_SHOP_ID + ", " +
                "350206, '回退测试地址', '测试用户', '13900001111', 3, 0, NOW())");
    }

    @Test
    public void testBackServiceOrder_Success() throws Exception {
        mockMvc.perform(put("/maintainers/{did}/services/{id}/cancel", TEST_MAINTAINER_ID, TEST_ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"维修无法完成，退回待派工\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));

        entityManager.flush();
        entityManager.clear();

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM service_service WHERE id = ?", Integer.class, TEST_ORDER_ID);
        org.junit.jupiter.api.Assertions.assertEquals(1, status);
    }

    @Test
    public void testBackServiceOrder_StateNotAllow() throws Exception {
        Long anotherId = TEST_ORDER_ID + 1;
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + anotherId);
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + anotherId + ", " + TEST_MAINTAINER_ID + ", " + TEST_SHOP_ID + ", " +
                "350206, '回退失败状态地址', '测试用户', '13900002222', 1, 0, NOW())");

        mockMvc.perform(put("/maintainers/{did}/services/{id}/cancel", TEST_MAINTAINER_ID, anotherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"状态不允许退回\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.STATENOTALLOW.getErrNo()));
    }

    @Test
    public void testBackServiceOrder_ResultNull() throws Exception {
        Long nullResultId = TEST_ORDER_ID + 2;
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = " + nullResultId);
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type, gmt_create) " +
                "VALUES (" + nullResultId + ", " + TEST_MAINTAINER_ID + ", " + TEST_SHOP_ID + ", " +
                "350206, '回退空结果地址', '测试用户', '13900003333', 3, 0, NOW())");

        mockMvc.perform(put("/maintainers/{did}/services/{id}/cancel", TEST_MAINTAINER_ID, nullResultId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // 未提供 result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errno").value(ReturnNo.FIELD_NOTVALID.getErrNo()));
    }
}