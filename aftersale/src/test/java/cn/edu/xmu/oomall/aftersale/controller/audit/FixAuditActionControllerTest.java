package cn.edu.xmu.oomall.aftersale.controller.audit;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AftersaleApplication.class)
@AutoConfigureMockMvc
@Transactional
@Import(FixAuditActionControllerTest.MockConfig.class)
public class FixAuditActionControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public RedisUtil redisUtil() { return Mockito.mock(RedisUtil.class); }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AftersaleOrderDao aftersaleOrderDao;

    // 使用 MockitoBean 接管 Feign 客户端调用
    @MockitoBean
    private ServiceOrderClient serviceOrderClient;

    private final Long targetId = 2L;

    @BeforeEach
    public void setup() {
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);
        if (po != null) {
            po.setType(2); // 维修类型
            po.setStatus(AftersaleOrder.UNAUDIT); // 初始状态：待审核 (0)
            aftersaleOrderDao.update(po);
        }
    }

    /**
     * 测试场景: 维修审核成功
     * 逻辑: 审核通过 -> 调用 ServiceOrderClient 成功 -> 状态变为已生成服务单 (3)
     */
    @Test
    public void auditAftersale_Fix_Success() throws Exception {
        // 1. 定义 Mock 行为：模拟远程服务返回成功，生成 ID 为 999 的服务单
        ServiceOrderResponseDTO mockResponse = new ServiceOrderResponseDTO();
        mockResponse.setId(999L);
        InternalReturnObject<ServiceOrderResponseDTO> ret = new InternalReturnObject<>(mockResponse);

        Mockito.when(serviceOrderClient.createServiceOrder(anyLong(), anyLong(), any(), any()))
                .thenReturn(ret);

        String requestBody = """
            {
              "confirm": true,
              "conclusion": "同意维修"
            }
            """;

        // 2. 发起 MockMvc 调用
        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0));

        // 3. 验证数据库
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);

        // 验证状态是否正确流转到 GENERATE_SERVICEORDER (3)
        assertEquals(AftersaleOrder.GENERATE_SERVICEORDER, updatedPo.getStatus());
        // 验证返回的服务单 ID 是否成功保存
        assertEquals(999L, updatedPo.getServiceOrderId());
    }
}