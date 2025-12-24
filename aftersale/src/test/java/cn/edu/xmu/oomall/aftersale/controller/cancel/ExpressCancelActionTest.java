package cn.edu.xmu.oomall.aftersale.controller.cancel;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 专门用于测试 ExpressCancelAction 集成流程的测试类
 * 场景： 待验收-> 顾客取消，此时应远程取消物流单
 */
@SpringBootTest(classes = AftersaleApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ExpressCancelActionTest.MockConfig.class)
@Rollback(true)
public class ExpressCancelActionTest {

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

    @MockitoBean
    private ExpressClient expressClient;

    private Long targetId;

    @BeforeEach
    public void setup() {
        targetId = 1L;
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);
        if (po == null) {
            System.err.println("警告：数据库中未找到 ID=1 的售后单，后续测试可能失败。请确保数据库已初始化。");
            return;
        }
        // 构造“已生成物流单”的售后单
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        bo.setType(1);                       // 类型只能是0/1
        bo.setStatus(AftersaleOrder.UNCHECK); // 待验收
        bo.setCustomerExpressId(9999L);              // 模拟已创建物流单
        bo.setCustomerName("测试用户李四");
        bo.setCustomerMobile("13987654321");

        AftersaleOrderPo toSavePo = CloneFactory.copy(po, bo);
        aftersaleOrderDao.update(toSavePo);
    }

    /**
     * 测试场景 1：取消成功 -> 远程物流取消成功，售后单状态变为已取消
     */
    @Test
    public void cancelAftersale_ExpressCancel_Success() throws Exception {
        // 1. Mock 物流取消成功
        PackageResponseDTO mockResp = new PackageResponseDTO(9999L, "SF_CANCEL_SUCCESS");
        InternalReturnObject<PackageResponseDTO> successRet = new InternalReturnObject<>(mockResp);
        Mockito.when(expressClient.cancelPackage(anyLong(), anyLong(), any()))
                .thenReturn(successRet);

        // 2. 发起取消请求
        mockMvc.perform(put("/aftersales/{id}", targetId)
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0))
                .andDo(print());

        // 3. 验证数据库状态
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.CANCEL, updatedPo.getStatus());
    }

    /**
     * 测试场景 2：远程物流取消失败 -> 取消操作整体失败，状态保持不动
     */
    @Test
    public void cancelAftersale_ExpressCancel_RemoteFail() throws Exception {
        // 1. Mock 物流侧返回失败
        InternalReturnObject<PackageResponseDTO> failRet = new InternalReturnObject<>();
        failRet.setErrno(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo());
        failRet.setErrmsg("物流系统取消报错");
        Mockito.when(expressClient.cancelPackage(anyLong(), anyLong(), any()))
                .thenReturn(failRet);

        // 2. 发起取消
        mockMvc.perform(put("/aftersales/{id}", targetId)
                        .header("authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()))
                .andDo(print());

        // 3. 验证状态未被修改
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.UNCHECK, updatedPo.getStatus());
    }
}