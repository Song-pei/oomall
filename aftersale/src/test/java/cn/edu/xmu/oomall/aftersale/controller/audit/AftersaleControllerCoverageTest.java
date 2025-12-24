package cn.edu.xmu.oomall.aftersale.controller.audit;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.ActionResult;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyProperties;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyRouter;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit.ExpressAuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.audit.FixAuditAction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AftersaleApplication.class)
@AutoConfigureMockMvc
@Import(AftersaleControllerCoverageTest.MockConfig.class)
public class AftersaleControllerCoverageTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        public RedisUtil redisUtil() {
            return Mockito.mock(RedisUtil.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AftersaleOrderService aftersaleOrderService;

    @MockitoBean
    private StrategyRouter strategyRouter;

    @MockitoBean
    private AftersaleOrderDao aftersaleOrderDao;

    @MockitoBean
    private ServiceOrderClient serviceOrderClient;

    @MockitoBean
    private ExpressClient expressClient;

    @Autowired
    private FixAuditAction fixAuditAction;

    @Autowired
    private ExpressAuditAction expressAuditAction;

    @Test
    public void searchAftersales_NoToken_TriggersMockUser() throws Exception {
        Mockito.when(aftersaleOrderService.searchAftersales(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/shops/1/aftersales")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0));
    }

    @Test
    public void auditAftersale_ConfirmNull_ReturnsError() throws Exception {
        Long targetId = 1L;
        String requestBody = "{ \"conclusion\": \"同意\" }";

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").isNotEmpty());
    }

    @Test
    public void auditAftersale_Catch_IllegalArgumentException() throws Exception {
        Long targetId = 1L;
        String requestBody = "{ \"confirm\": true, \"conclusion\": \"同意测试\", \"reason\": \"合规测试\" }";

        Mockito.doThrow(new IllegalArgumentException("参数错误测试"))
                .when(aftersaleOrderService).audit(any(), any(), any(), any(), any(), any());

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errno").value(ReturnNo.FIELD_NOTVALID.getErrNo()));
    }

    @Test
    public void auditAftersale_Catch_IllegalStateException() throws Exception {
        Long targetId = 1L;
        String requestBody = "{ \"confirm\": true, \"conclusion\": \"同意测试\", \"reason\": \"合规测试\" }";

        Mockito.doThrow(new IllegalStateException("状态不对测试"))
                .when(aftersaleOrderService).audit(any(), any(), any(), any(), any(), any());

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.STATENOTALLOW.getErrNo()));
    }

    @Test
    public void auditAftersale_BusinessException_NotFound() throws Exception {
        Long targetId = 999L;
        String requestBody = "{ \"confirm\": true, \"conclusion\": \"同意\", \"reason\": \"测试\" }";

        Mockito.doThrow(new BusinessException(ReturnNo.RESOURCE_ID_NOTEXIST, "售后单不存在"))
                .when(aftersaleOrderService).audit(any(), any(), any(), any(), any(), any());

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }

    @Test
    public void auditAftersale_BusinessException_PermissionDenied() throws Exception {
        Long targetId = 1L;
        String requestBody = "{ \"confirm\": true, \"conclusion\": \"同意\", \"reason\": \"测试\" }";

        Mockito.doThrow(new BusinessException(ReturnNo.RESOURCE_ID_OUTSCOPE, "无权操作"))
                .when(aftersaleOrderService).audit(any(), any(), any(), any(), any(), any());

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }

    @Test
    public void auditAftersale_UnexpectedError() throws Exception {
        Long targetId = 1L;
        String requestBody = "{ \"confirm\": true, \"conclusion\": \"同意\", \"reason\": \"测试\" }";

        Mockito.doThrow(new RuntimeException("数据库连接断开"))
                .when(aftersaleOrderService).audit(any(), any(), any(), any(), any(), any());

        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }

    @Test
    public void bo_Coverage_SetModifier() {
        AftersaleOrder bo = new AftersaleOrder();
        UserToken user = new UserToken();
        user.setId(88L);
        user.setName("Tester");

        bo.setModifier(user);
        assertEquals(88L, bo.getModifierId());
        assertNotNull(bo.getGmtModified());

        bo.setModifier(null);
        assertEquals(0L, bo.getModifierId());
        assertEquals("System", bo.getModifierName());
    }

    @Test
    public void bo_Coverage_StatusNotAllow() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setStatus(AftersaleOrder.UNCHECK);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bo.audit("ok", "ok", true, strategyRouter)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }

    @Test
    public void bo_Coverage_StrategyNotFound() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setType(1);
        bo.setStatus(AftersaleOrder.UNAUDIT);

        Mockito.when(strategyRouter.route(any(), any(), any(), any())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bo.audit("同意", "原因", true, strategyRouter)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }

    @Test
    public void bo_Coverage_TransitionFailed() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setType(1);
        bo.setStatus(AftersaleOrder.UNAUDIT);

        AuditAction mockAction = Mockito.mock(AuditAction.class);
        Mockito.when(mockAction.execute(any(), any())).thenReturn(ActionResult.status(999));
        Mockito.when(strategyRouter.route(any(), any(), any(), any())).thenReturn(mockAction);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bo.audit("同意", "原因", true, strategyRouter)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }

    @Test
    public void action_Fix_RemoteReturnError() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setId(101L);
        bo.setShopId(1L);

        InternalReturnObject<ServiceOrderResponseDTO> failRet = new InternalReturnObject<>(500, "Mock Error");
        Mockito.when(serviceOrderClient.createServiceOrder(any(), any(), any(), any()))
                .thenReturn(failRet);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                fixAuditAction.execute(bo, "pass")
        );
        assertEquals(ReturnNo.REMOTE_SERVICE_FAIL, ex.getErrno());
    }

    @Test
    public void action_Fix_RemoteThrowException() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setId(101L);

        Mockito.when(serviceOrderClient.createServiceOrder(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Network Timeout"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                fixAuditAction.execute(bo, "pass")
        );
        assertEquals(ReturnNo.REMOTE_SERVICE_FAIL, ex.getErrno());
    }

    @Test
    public void action_Express_RemoteReturnError() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setId(202L);
        bo.setShopId(1L);
        bo.setCustomerRegionId(10L);

        InternalReturnObject<PackageResponseDTO> failRet = new InternalReturnObject<>(500, "Express Error");
        Mockito.when(expressClient.createPackage(any(), any(), any()))
                .thenReturn(failRet);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                expressAuditAction.execute(bo, "pass")
        );
        assertEquals(ReturnNo.REMOTE_SERVICE_FAIL, ex.getErrno());
    }

    @Test
    public void action_Express_RemoteThrowException() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setId(202L);
        bo.setCustomerRegionId(10L);

        Mockito.when(expressClient.createPackage(any(), any(), any()))
                .thenThrow(new RuntimeException("Feign Error"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                expressAuditAction.execute(bo, "pass")
        );
        assertEquals(ReturnNo.REMOTE_SERVICE_FAIL, ex.getErrno());
    }

    @Test
    public void router_Init_BeanNotFound() {
        StrategyRouter manualRouter = new StrategyRouter();
        ApplicationContext mockCtx = Mockito.mock(ApplicationContext.class);
        StrategyProperties mockProps = Mockito.mock(StrategyProperties.class);

        StrategyProperties.Rule badRule = new StrategyProperties.Rule();
        badRule.setBeanName("ghostBean");
        badRule.setType(1);
        badRule.setStatus(1);
        badRule.setOpt("AUDIT");

        List<StrategyProperties.Rule> ruleList = new ArrayList<>();
        ruleList.add(badRule);

        Mockito.when(mockProps.getStrategies()).thenReturn(ruleList);
        Mockito.when(mockCtx.containsBean("ghostBean")).thenReturn(false);

        ReflectionTestUtils.setField(manualRouter, "applicationContext", mockCtx);
        ReflectionTestUtils.setField(manualRouter, "strategyProperties", mockProps);

        manualRouter.init();

        Object result = manualRouter.route(1, 1, "AUDIT", Object.class);
        assertNull(result);
    }

    @Test
    public void router_Route_TypeMismatch() {
        StrategyRouter manualRouter = new StrategyRouter();
        ApplicationContext mockCtx = Mockito.mock(ApplicationContext.class);
        StrategyProperties mockProps = Mockito.mock(StrategyProperties.class);

        StrategyProperties.Rule stringRule = new StrategyProperties.Rule();
        stringRule.setBeanName("stringBean");
        stringRule.setType(2);
        stringRule.setStatus(2);
        stringRule.setOpt("TEST");

        List<StrategyProperties.Rule> ruleList = new ArrayList<>();
        ruleList.add(stringRule);

        Mockito.when(mockProps.getStrategies()).thenReturn(ruleList);
        Mockito.when(mockCtx.containsBean("stringBean")).thenReturn(true);
        Mockito.when(mockCtx.getBean("stringBean")).thenReturn("I am a String, not an Action");

        ReflectionTestUtils.setField(manualRouter, "applicationContext", mockCtx);
        ReflectionTestUtils.setField(manualRouter, "strategyProperties", mockProps);

        manualRouter.init();

        Integer result = manualRouter.route(2, 2, "TEST", Integer.class);
        assertNull(result);
    }

    @Test
    public void router_Route_KeyNotFound() {
        StrategyRouter cleanRouter = new StrategyRouter();
        StrategyProperties mockProps = Mockito.mock(StrategyProperties.class);

        Mockito.when(mockProps.getStrategies()).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(cleanRouter, "strategyProperties", mockProps);

        cleanRouter.init();

        Object result = cleanRouter.route(999, 999, "NOWHERE", Object.class);
        assertNull(result);
    }

    @Test
    public void service_Audit_ResourceNotExist() {
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);
        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);
        Mockito.when(aftersaleOrderDao.findById(any())).thenReturn(null);

        UserToken user = new UserToken();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                manualService.audit(100L, 1L, true, "pass", "ok", user)
        );
        assertEquals(ReturnNo.RESOURCE_ID_NOTEXIST, ex.getErrno());
    }

    @Test
    public void service_Audit_ShopIdMismatch() {
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);
        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);

        AftersaleOrderPo mockPo = new AftersaleOrderPo();
        mockPo.setId(1L);
        mockPo.setShopId(100L);
        mockPo.setStatus(AftersaleOrder.UNAUDIT);
        Mockito.when(aftersaleOrderDao.findById(any())).thenReturn(mockPo);

        UserToken user = new UserToken();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                manualService.audit(200L, 1L, true, "pass", "ok", user)
        );
        assertEquals(ReturnNo.RESOURCE_ID_OUTSCOPE, ex.getErrno());
    }

    @Test
    public void service_Audit_StatusNotAllow() {
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);
        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);

        AftersaleOrderPo mockPo = new AftersaleOrderPo();
        mockPo.setId(1L);
        mockPo.setShopId(100L);
        mockPo.setStatus(AftersaleOrder.FINISH);
        Mockito.when(aftersaleOrderDao.findById(any())).thenReturn(mockPo);

        UserToken user = new UserToken();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                manualService.audit(100L, 1L, true, "pass", "ok", user)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }
}