package cn.edu.xmu.oomall.service.controller;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderFinishDto;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.mapper.ServiceOrderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest(classes = ServiceApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                // 注入测试所需的策略路由
                "ser-vice.strategies[0].type=0",
                "ser-vice.strategies[0].status=3",
                "ser-vice.strategies[0].opt=FINISH",
                "ser-vice.strategies[0].bean-name=simpleFinishAction",
                "ser-vice.strategies[1].type=1",
                "ser-vice.strategies[1].status=3",
                "ser-vice.strategies[1].opt=FINISH",
                "ser-vice.strategies[1].bean-name=expressFinishAction"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ServiceOrderControllerTest.MockConfig.class)
@Rollback(true)
@DisplayName("服务单完成接口测试")
class ServiceOrderControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public RedisUtil redisUtil() {
            return Mockito.mock(RedisUtil.class);
        }
        @Bean
        public ExpressClient expressClient() {
            return Mockito.mock(ExpressClient.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExpressClient expressClient;
    @Autowired
    private ServiceOrderPoMapper serviceOrderPoMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @PersistenceContext
    private EntityManager entityManager;

    private static final String FINISH_URL = "/maintainers/{did}/services/{id}/finish";
    private final Long MAINTAINER_ID = 1L;
    private final Long SHOP_ID = 100L;

    /**
     * 辅助方法：准备数据库数据
     */
    private ServiceOrderPo createValidPo(Byte type, Byte status, Long maintainerId) {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setType(type);
        po.setStatus(status);
        po.setMaintainerId(maintainerId);
        po.setShopId(SHOP_ID);
        po.setRegionId(1101L);
        po.setAddress("望海路10号");
        po.setConsignee("sean");
        po.setMobile("13888888888");
        po.setGmtCreate(LocalDateTime.now());
        ServiceOrderPo saved = serviceOrderPoMapper.saveAndFlush(po);
        entityManager.clear();
        return saved;
    }

    @Test
    @DisplayName("场景1：上门维修成功完成")
    void finishOnSite_Success() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING, MAINTAINER_ID);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("维修完成");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));
    }

    @Test
    @DisplayName("场景2：寄件维修成功完成 - Feign调用成功")
    void finishExpress_Success() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 1, ServiceOrder.REPAIRING, MAINTAINER_ID);

        ExpressPo mockExpress = new ExpressPo();
        mockExpress.setId(999L);
        mockExpress.setBillCode("SF_MOCK_123");
        InternalReturnObject<ExpressPo> ret = new InternalReturnObject<>(mockExpress);

        Mockito.reset(expressClient);
        Mockito.when(expressClient.createPackage(eq(SHOP_ID), any(ExpressDto.class), any(), any()))
                .thenReturn(ret);

        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("已寄出");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));
    }

    @Test
    @DisplayName("场景3：远程服务异常 - Feign抛出异常")
    void finishExpress_FeignException() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 1, ServiceOrder.REPAIRING, MAINTAINER_ID);

        Mockito.reset(expressClient);
        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Connect Timeout"));

        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("测试异常");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo())));
    }

    @Test
    @DisplayName("场景4：策略缺失 - 未配置type=2的完成策略")
    void finish_StrategyNotFound() throws Exception {
        // 创建 type=2 的订单，但在 properties 中未配置 type=2 的策略
        ServiceOrderPo po = createValidPo((byte) 2, ServiceOrder.REPAIRING, MAINTAINER_ID);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("无效策略测试");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.STATENOTALLOW.getErrNo())));
    }

    @Test
    @DisplayName("场景5：越权访问 - did与单据maintainerId不匹配")
    void finish_Forbidden() throws Exception {
        // 1. 准备数据：属于服务商 1L 的单子
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING, 1L);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("越权测试");

        // 2. 执行请求：使用 did=99L 去访问
        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, 99L, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // 3. 断言：如果 Service 层加了校验，这里应该返回 403 Forbidden
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.RESOURCE_ID_OUTSCOPE.getErrNo())));
    }

    @Test
    @DisplayName("场景6：参数校验失败 - result为空白字符串")
    void finish_InvalidParam() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING, MAINTAINER_ID);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("   "); // 空白字符

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.FIELD_NOTVALID.getErrNo())));
    }
}