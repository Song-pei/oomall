package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageCreateDTO;
import cn.edu.xmu.oomall.aftersale.controller.dto.PackageResponseDTO;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.feign.ExpressClient;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 专门用于测试 ExpressAuditAction 集成流程的测试类
 */
@SpringBootTest(classes = AftersaleApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ExpressAuditActionControllerTest.MockConfig.class)
@Rollback(true)
public class ExpressAuditActionControllerTest {

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
    private AftersaleOrderDao aftersaleOrderDao;

    // Mock 远程 Feign 客户端
    @MockitoBean
    private ExpressClient expressClient;

    private Long targetId;

    @BeforeEach
    public void setup() {
        // 1. 准备数据
        targetId = 1L;
        // 使用 PO 接收数据库查询结果
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);

        if (po != null) {
            // 将 PO 转为 BO 以便修改业务字段
            AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);

            // 强制设置为 type=1 (退货/换货) 且 status=待审核
            bo.setType(1);
            bo.setStatus(AftersaleOrder.UNAUDIT);
            bo.setCustomerName("测试用户张三");
            bo.setCustomerMobile("13912345678");

            // 将修改后的 BO 拷回 PO
            AftersaleOrderPo toSavePo = CloneFactory.copy(po, bo);

            // 调用 DAO 更新
            aftersaleOrderDao.update(toSavePo);
        } else {
            System.err.println("警告：数据库中未找到 ID=1 的售后单，后续测试可能失败。请确保数据库已初始化。");
        }
    }

    /**
     * 测试场景 1: 审核通过 (confirm=true) -> 成功创建运单并保存 expressId
     */
    @Test
    public void auditAftersale_Type1_Success() throws Exception {
        // 1. 定义 Mock 行为
        Long expectedExpressId = 10086L; // 预期的物流单 ID
        String expectedBillCode = "SF_TEST_SUCCESS";
        PackageResponseDTO mockResponse = new PackageResponseDTO(expectedExpressId, expectedBillCode);
        InternalReturnObject<PackageResponseDTO> successRet = new InternalReturnObject<>(mockResponse);

        Mockito.when(expressClient.createPackage(anyLong(), any(PackageCreateDTO.class), any()))
                .thenReturn(successRet);

        // 2. 构造请求参数
        String requestBody = """
                    {
                        "confirm": true,
                        "conclusion": "同意退货",
                        "reason": ""
                    }
                """;

        // 3. 发起调用
        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());

        // 4. 验证数据库状态更新
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);

        // 验证基本状态
        assertEquals(AftersaleOrder.UNCHECK.byteValue(), updatedPo.getStatus());
        assertEquals("同意", updatedPo.getConclusion());

        // 验证 express_id 是否成功从 Feign 返回值保存到了数据库
        assertNotNull(updatedPo.getExpressId(), "express_id 应该被保存到数据库中");
        assertEquals(expectedExpressId, updatedPo.getExpressId(), "数据库中的 express_id 应与 Mock 返回的一致");
    }


    /**
     * 测试场景 2: 远程物流服务报错 -> 审核失败
     */
    @Test
    public void auditAftersale_Type1_RemoteFail() throws Exception {
        // 1. Mock
        InternalReturnObject<PackageResponseDTO> failRet = new InternalReturnObject<>();
        failRet.setErrno(ReturnNo.INTERNAL_SERVER_ERR.getErrNo());
        failRet.setErrmsg("物流系统繁忙");

        Mockito.when(expressClient.createPackage(
                anyLong(),
                any(PackageCreateDTO.class),
                any()
        )).thenReturn(failRet);

        String requestBody = """
            { "confirm": true, "conclusion": "同意", "reason": "测试失败场景" }
        """;

        // 2. 发起调用
        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isInternalServerError())

                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }
}