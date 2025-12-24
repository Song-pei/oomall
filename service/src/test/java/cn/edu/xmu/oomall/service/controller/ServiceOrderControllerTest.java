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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
@Rollback(true)
@DisplayName("服务单完成接口集成测试")
class ServiceOrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ServiceOrderPoMapper serviceOrderPoMapper;
    @Autowired private ObjectMapper objectMapper;
    @PersistenceContext private EntityManager entityManager;

    @MockitoBean private ExpressClient expressClient;
    @MockitoBean private RedisUtil redisUtil;

    private static final String FINISH_URL = "/maintainers/{did}/services/{id}/finish";
    private final Long MAINTAINER_ID = 1L;
    private final Long SHOP_ID = 100L;

    private ServiceOrderPo createValidPo(Byte type, Byte status) {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setType(type);
        po.setStatus(status);
        po.setMaintainerId(MAINTAINER_ID);
        po.setShopId(SHOP_ID);
        po.setRegionId(1101L);
        po.setAddress("厦门大学信息学院");
        po.setConsignee("sean");
        po.setMobile("13800000000");
        po.setGmtCreate(LocalDateTime.now());
        ServiceOrderPo saved = serviceOrderPoMapper.saveAndFlush(po);
        entityManager.clear();
        return saved;
    }

    @Test
    @DisplayName("场景1：上门维修(type=0)成功完成")
    void finishOnSite_Success() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING);
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
    @DisplayName("场景2：寄件维修(type=1)成功完成")
    void finishExpress_Success() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 1, ServiceOrder.REPAIRING);

        ExpressPo mockExpress = new ExpressPo();
        mockExpress.setId(999L);
        mockExpress.setBillCode("SF_123456");
        InternalReturnObject<ExpressPo> ret = new InternalReturnObject<>(mockExpress);

        // ✅ 核心修正：使用 any() 匹配所有后续参数，确保 Mock 绝对生效
        Mockito.when(expressClient.createPackage(
                eq(SHOP_ID),
                any(ExpressDto.class),
                any(), // 对应 String user
                any()  // 对应 Integer userLevel
        )).thenReturn(ret);

        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("已寄出维修件");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.OK.getErrNo())));
    }

    @Test
    @DisplayName("场景3：远程服务异常")
    void finishExpress_FeignException() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 1, ServiceOrder.REPAIRING);

        // ✅ 使用 any() 确保异常抛出逻辑被触发
        Mockito.when(expressClient.createPackage(
                anyLong(),
                any(ExpressDto.class),
                any(),
                any()
        )).thenThrow(new RuntimeException("Connect Timeout"));

        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("测试异常场景");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo())));
    }

    @Test
    @DisplayName("场景5：越权访问")
    void finish_Forbidden() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("越权访问测试");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, 99L, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.RESOURCE_ID_OUTSCOPE.getErrNo())));
    }

    @Test
    @DisplayName("场景6：参数校验失败")
    void finish_InvalidParam() throws Exception {
        ServiceOrderPo po = createValidPo((byte) 0, ServiceOrder.REPAIRING);
        ServiceOrderFinishDto dto = new ServiceOrderFinishDto();
        dto.setResult("   ");

        this.mockMvc.perform(MockMvcRequestBuilders.post(FINISH_URL, MAINTAINER_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(3)));
    }
}