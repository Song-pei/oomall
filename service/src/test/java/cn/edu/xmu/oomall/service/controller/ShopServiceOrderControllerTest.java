package cn.edu.xmu.oomall.service.controller;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.oomall.service.ServiceApplication;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.mapper.ServiceOrderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceOrderPo;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.controller.dto.ExpressDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

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
@DisplayName("商铺服务单查询接口测试")
public class ShopServiceOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceOrderPoMapper serviceOrderPoMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpressClient expressClient;

    @MockitoBean
    private RedisUtil redisUtil;

    private final String GET_URL = "/shops/{did}/services/{id}";
    private final Long SHOP_ID = 100L;

    /**
     * ✅ 在每个测试前重置 Mock 状态
     */
    @BeforeEach
    void setUp() {
        Mockito.reset(expressClient, redisUtil);
        
        // ✅ 默认 Mock：createPackage 返回成功
        ExpressPo mockExpressPo = new ExpressPo();
        mockExpressPo.setId(999L);
        mockExpressPo.setBillCode("SF_123456");
        
        InternalReturnObject<ExpressPo> successResult = new InternalReturnObject<>();
        successResult.setErrno(0);
        successResult.setData(mockExpressPo);
        
        Mockito.when(expressClient.createPackage(
                Mockito.anyLong(),
                Mockito.any(ExpressDto.class),
                Mockito.anyString(),
                Mockito.anyInt()
        )).thenReturn(successResult);
    }

    private ServiceOrderPo createPo(Long id, Long shopId, Byte status, String result, Long expressId) {
        ServiceOrderPo po = new ServiceOrderPo();
        po.setId(id);
        po.setShopId(shopId);
        po.setStatus(status);
        po.setResult(result);
        po.setExpressId(expressId);
        po.setConsignee("张三");
        po.setRegionId(1101L);
        po.setAddress("厦门大学信息学院");
        po.setMobile("13900139000");
        po.setType((byte) 0);
        po.setGmtCreate(LocalDateTime.now());
        ServiceOrderPo saved = serviceOrderPoMapper.saveAndFlush(po);
        entityManager.clear();
        return saved;
    }

    // ========== 查询接口测试 ==========

    @Test
    @DisplayName("用例1: 查询正常的服务单 - 成功")
    void getServiceOrderById_Success() throws Exception {
        ServiceOrderPo po = createPo(1001L, SHOP_ID, ServiceOrder.REPAIRING, null, null);

        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.OK.getErrNo())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id", is(1001)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.shopId", is(100)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.consignee", is("张三")));
    }

    @Test
    @DisplayName("用例2: 查询已完成的服务单 - 成功 (验证运单号)")
    void getServiceOrderById_Finished() throws Exception {
        ServiceOrderPo po = createPo(1002L, SHOP_ID, ServiceOrder.FINISH, "维修完成，已寄回", 888L);

        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status", is((int) ServiceOrder.FINISH)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.expressId", is(888)));
    }

    @Test
    @DisplayName("用例3: 查询不属于该商铺的服务单 - 权限不足 (errno 17)")
    void getServiceOrderById_Forbidden() throws Exception {
        ServiceOrderPo po = createPo(1003L, 200L, ServiceOrder.REPAIRING, null, null);

        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.RESOURCE_ID_OUTSCOPE.getErrNo())));
    }

    @Test
    @DisplayName("用例4: 查询不存在的服务单 - 资源不存在 (errno 4)")
    void getServiceOrderById_NotFound() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, 99999L)
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errno", is(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo())));
    }

    @Test
    @DisplayName("用例5: 查询待接受的服务单 - 验证 result 为空")
    void getServiceOrderById_Unaccepted() throws Exception {
        ServiceOrderPo po = createPo(1004L, SHOP_ID, ServiceOrder.UNCHECK, null, null);

        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, po.getId())
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status", is((int) ServiceOrder.UNCHECK)))
                // ✅ 修正：使用 doesNotExist() 而非 value(nullValue())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.result").doesNotExist());
    }

    @Test
    @DisplayName("用例8: 非法的 ID 类型 - 400")
    void getServiceOrderById_InvalidId() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get(GET_URL, SHOP_ID, "abc")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("用例7: 错误的 HTTP 方法 - 405")
    void getServiceOrderById_WrongMethod() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post(GET_URL, SHOP_ID, 1001L)
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

}