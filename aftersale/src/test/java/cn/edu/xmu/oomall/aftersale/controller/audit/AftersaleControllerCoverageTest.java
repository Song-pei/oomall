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
// [修正] 使用正确的 PO 路径
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
import cn.edu.xmu.oomall.aftersale.service.feign.ServiceOrderClient;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
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

    // 核心依赖 (Mock)
    @MockitoBean
    private AftersaleOrderService aftersaleOrderService;

    @MockitoBean
    private StrategyRouter strategyRouter;

    @MockitoBean
    private AftersaleOrderDao aftersaleOrderDao;

    // 远程客户端 (Mock)
    @MockitoBean
    private ServiceOrderClient serviceOrderClient;

    @MockitoBean
    private ExpressClient expressClient;

    // 真实的 Action 组件
    @Autowired
    private FixAuditAction fixAuditAction;

    @Autowired
    private ExpressAuditAction expressAuditAction;


    // =======================================================
    // Part 1: Controller 层测试 (MockMvc -> Mock Service)
    // =======================================================

    /* 测试：未携带 Token 时的搜索请求，验证是否触发默认 Mock 用户逻辑 */
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

    /* 测试：审核请求中缺少 confirm 字段，应返回错误 */
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

    /* 测试：捕获 Service 抛出的参数异常 (IllegalArgumentException) */
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

    /* 测试：捕获 Service 抛出的状态异常 (IllegalStateException) */
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

    /* 测试：捕获业务异常 - 资源不存在 (RESOURCE_ID_NOTEXIST) */
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

    /* 测试：捕获业务异常 - 无权限 (RESOURCE_ID_OUTSCOPE) */
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

    /* 测试：捕获未预期的运行时异常，应返回服务器内部错误 */
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


    // =======================================================
    // Part 2: BO (业务对象) 逻辑测试
    // =======================================================

    /* 测试 BO：setModifier 方法，覆盖 UserToken 为空和不为空的场景 */
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

    /* 测试 BO：非待审核状态下尝试审核，应抛出状态不允许异常 */
    @Test
    public void bo_Coverage_StatusNotAllow() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setStatus(AftersaleOrder.UNCHECK);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bo.audit("ok", "ok", true, strategyRouter)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }

    /* 测试 BO：找不到对应的审核策略时，应抛出异常 */
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

    /* 测试 BO：审核动作返回非法状态导致流转失败 */
    @Test
    public void bo_Coverage_TransitionFailed() {
        AftersaleOrder bo = new AftersaleOrder();
        bo.setType(1);
        bo.setStatus(AftersaleOrder.UNAUDIT);

        AuditAction mockAction = Mockito.mock(AuditAction.class);
        Mockito.when(mockAction.execute(any(), any())).thenReturn(999);
        Mockito.when(strategyRouter.route(any(), any(), any(), any())).thenReturn(mockAction);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bo.audit("同意", "原因", true, strategyRouter)
        );
        assertEquals(ReturnNo.STATENOTALLOW, ex.getErrno());
    }


    // =======================================================
    // Part 3: Strategy/Action 组件异常分支测试 (Feign 客户端)
    // =======================================================

    /* 测试 FixAuditAction：远程服务返回业务错误码 (errno != 0) */
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

    /* 测试 FixAuditAction：远程调用抛出网络/运行时异常 */
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

    /* 测试 ExpressAuditAction：物流服务返回业务错误码 */
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

    /* 测试 ExpressAuditAction：物流服务远程调用抛出异常 */
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


    // =======================================================
    // Part 4: StrategyRouter 内部逻辑测试 (使用反射)
    // =======================================================

    /* 测试 StrategyRouter：初始化时配置了不存在的 Bean，应跳过 */
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

    /* 测试 StrategyRouter：路由找到 Bean 但类型不匹配，应返回 null */
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

    /* 测试 StrategyRouter：路由不存在的 Key，应返回 null */
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


    // =======================================================
    // Part 5: Service 内部逻辑测试 (手动实例化 Service)
    // [覆盖] if (po == null), if (shopId mismatch), if (status != UNAUDIT)
    // =======================================================

    /* 测试 Service Audit：数据库查不到单据 (po == null)，应抛出 RESOURCE_ID_NOTEXIST */
    @Test
    public void service_Audit_ResourceNotExist() {
        // [修复] 传入 null, null 满足构造函数要求 (假设它有2个依赖，如 Dao 和 Router)
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);

        // 2. 注入 Mock Dao
        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);

        // 3. Mock Dao 返回 null
        Mockito.when(aftersaleOrderDao.findById(any())).thenReturn(null);

        UserToken user = new UserToken();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                manualService.audit(100L, 1L, true, "pass", "ok", user) // shopId, id
        );
        assertEquals(ReturnNo.RESOURCE_ID_NOTEXIST, ex.getErrno());
    }

    /* 测试 Service Audit：店铺 ID 不匹配，应抛出 RESOURCE_ID_OUTSCOPE */
    @Test
    public void service_Audit_ShopIdMismatch() {
        // [修复] 传入 null, null
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);

        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);

        // 模拟数据库里存在的 PO (ShopId = 100)
        AftersaleOrderPo mockPo = new AftersaleOrderPo();
        mockPo.setId(1L);
        mockPo.setShopId(100L);
        mockPo.setStatus(AftersaleOrder.UNAUDIT);
        Mockito.when(aftersaleOrderDao.findById(any())).thenReturn(mockPo);

        // 尝试用 ShopId = 200 去审核
        UserToken user = new UserToken();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                manualService.audit(200L, 1L, true, "pass", "ok", user) // shopId=200 不匹配
        );
        assertEquals(ReturnNo.RESOURCE_ID_OUTSCOPE, ex.getErrno());
    }

    /* 测试 Service Audit：单据状态不是待审核，应抛出 STATENOTALLOW */
    @Test
    public void service_Audit_StatusNotAllow() {
        // [修复] 传入 null, null
        AftersaleOrderService manualService = new AftersaleOrderService(null, null);

        ReflectionTestUtils.setField(manualService, "aftersaleOrderDao", aftersaleOrderDao);

        // 模拟数据库单据状态为 FINISH (已完成)
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
