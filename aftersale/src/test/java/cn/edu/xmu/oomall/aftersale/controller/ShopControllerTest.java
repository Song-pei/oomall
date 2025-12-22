package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AftersaleApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ShopControllerTest.MockConfig.class)
@Rollback(false)
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

    @Test
    public void getAftersales() throws Exception {
        mockMvc.perform(get("/shops/1/aftersales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }

    @Test
    public void auditAftersale() throws Exception {
        String requestBody = "{\"confirm\": true, \"conclusion\": \"同意\", \"reason\": \"质量问题\"}";

        mockMvc.perform(put("/shops/1/aftersales/1/confirm")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());
    }
//测试审核售后单时，缺少confirm字段的情况
    @Test
    public void auditAftersale_missingConfirm() throws Exception {
        String requestBody = "{\"conclusion\": \"同意退款\", \"reason\": \"质量问题\"}";

        mockMvc.perform(put("/shops/1/aftersales/1/confirm")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(706)) // AFTERSALE_AUDIT_RESULT_EMPTY
                .andDo(print());
    }
//测试审核售后单时，缺少token的情况
    @Test
    public void auditAftersale_noToken() throws Exception {
        String requestBody = "{\"confirm\": true, \"conclusion\": \"同意退款\", \"reason\": \"质量问题\"}";

        mockMvc.perform(put("/shops/1/aftersales/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(707)) // AFTERSALE_NOT_LOGIN
                .andDo(print());
    }
}
