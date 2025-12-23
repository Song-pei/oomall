package cn.edu.xmu.oomall.aftersale.controller.cancel;

import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceFind;
import cn.edu.xmu.oomall.aftersale.controller.dto.ServiceOrderCancelDTO;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 测试 ServiceCancelAction 集成流程
 * 场景：维修单已生成服务单，顾客取消，需远程调用服务模块取消服务单
 */
@SpringBootTest(classes = AftersaleApplication.class,
        properties = {
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false"
        })
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRED)
@Import(ServiceCancelActionControllerTest.MockConfig.class)
@Rollback(true)
public class ServiceCancelActionControllerTest {

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
    private ServiceOrderClient serviceOrderClient;

    private Long targetId;
    private static final Long MAINTAINER_ID = 9999L; // 模拟的服务商ID

    @BeforeEach
    public void setup() {
        targetId = 1L;
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);
        if (po == null) {
            System.err.println("警告：数据库中未找到 ID=1 的售后单，后续测试可能失败。请确保数据库已初始化。");
            return;
        }
        // 构造“维修+已生成服务单”状态的售后单
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        bo.setType(2);                                    // 维修
        bo.setStatus(AftersaleOrder.GENERATE_SERVICEORDER); // 已生成服务单
        bo.setServiceOrderId(8888L);                      // 已关联服务单
        bo.setShopId(1L);                                 // 与 URI 中的 shopId 保持一致
        bo.setCustomerId(1001L);
        bo.setCustomerName("维修用户赵六");
        bo.setCustomerMobile("13966668888");

        AftersaleOrderPo toSavePo = CloneFactory.copy(po, bo);
        aftersaleOrderDao.update(toSavePo);
    }

    /**
     * 场景1：服务单取消成功
     */
    @Test
    public void cancelAftersale_ServiceCancel_Success() throws Exception {
        // 构造获取服务商ID的远程返回对象
        ServiceFind serviceFind = new ServiceFind();
        ServiceFind.Maintainer maintainer;
        maintainer = serviceFind.new Maintainer();
        maintainer.setId(MAINTAINER_ID);
        serviceFind.setMaintainer(maintainer);
        InternalReturnObject<ServiceFind> findRet = new InternalReturnObject<>();
        findRet.setErrno(0);
        findRet.setErrmsg("成功");
        findRet.setData(serviceFind);
        //  构造取消服务单的远程返回对象
        ServiceOrderResponseDTO mockResp = new ServiceOrderResponseDTO();
        mockResp.setId(8888L);
        InternalReturnObject<ServiceOrderResponseDTO> successRet = new InternalReturnObject<>();
        successRet.setErrno(0);
        successRet.setErrmsg("成功");
        successRet.setData(mockResp);

        // 3. 打桩远程调用
        Mockito.when(serviceOrderClient.getServiceOrder(
                eq(1L),           // shopId
                eq(8888L),        // serviceOrderId
                anyString()       // token
        )).thenReturn(findRet);

        Mockito.when(serviceOrderClient.customerCancelServiceOrder(
                eq(MAINTAINER_ID),
                eq(8888L),        // serviceOrderId
                anyString(),      // token
                any(ServiceOrderCancelDTO.class)
        )).thenReturn(successRet);

        // 3. 发请求
        mockMvc.perform(
                        put("/aftersales/{id}", targetId)
                                .header("authorization", "Bearer test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()))
                .andDo(print());

        // 4. 验数据库
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.CANCEL, updatedPo.getStatus());
    }

    /**
     *  场景2：获取服务商ID失败 -> 整体失败
     */
    @Test
    public void cancelAftersale_ServiceCancel_RemoteFail1() throws Exception {

        // 1. 构造获取服务商ID失败的返回
        InternalReturnObject<ServiceFind> findRet = new InternalReturnObject<>();
        findRet.setErrno(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo());
        findRet.setErrmsg("获取服务单信息失败");
       // 2. 打桩远程调用
        Mockito.when(serviceOrderClient.getServiceOrder(
                eq(1L),
                eq(8888L),
                anyString()
        )).thenReturn(findRet);
        // 3. 发请求
        mockMvc.perform(
                        put("/aftersales/{id}", targetId)
                                .header("authorization", "Bearer test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()))
                .andDo(print());
        // 4. 验数据库（状态应保持原样）
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.GENERATE_SERVICEORDER.byteValue(), updatedPo.getStatus());

    }
    /**
     * 场景3：远程取消服务单失败 -> 整体失败，状态不变
     */
    @Test
    public void cancelAftersale_ServiceCancel_RemoteFail2() throws Exception {
        // 构造获取服务商ID的远程返回对象
        ServiceFind serviceFind = new ServiceFind();
        ServiceFind.Maintainer maintainer;
        maintainer = serviceFind.new Maintainer();
        maintainer.setId(MAINTAINER_ID);
        serviceFind.setMaintainer(maintainer);
        InternalReturnObject<ServiceFind> findRet = new InternalReturnObject<>();
        findRet.setErrno(0);
        findRet.setErrmsg("成功");
        findRet.setData(serviceFind);

        // 构造取消服务单失败的返回
        InternalReturnObject<ServiceOrderResponseDTO> failRet = new InternalReturnObject<>();
        failRet.setErrno(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()); // 设置非0错误码
        failRet.setErrmsg("服务系统取消失败");

        // 打桩远程调用，模拟远程服务调用失败
        Mockito.when(serviceOrderClient.getServiceOrder(
                eq(1L),           // shopId
                eq(8888L),        // serviceOrderId
                anyString()       // token
        )).thenReturn(findRet);

        Mockito.when(serviceOrderClient.customerCancelServiceOrder(
                eq(MAINTAINER_ID),
                eq(8888L),        // serviceOrderId
                anyString(),      // token
                any(ServiceOrderCancelDTO.class)
        )).thenReturn(failRet);

        // 3. 发请求
        mockMvc.perform(
                        put("/aftersales/{id}", targetId)
                                .header("authorization", "Bearer test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errno").value(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo()))
                .andDo(print());


        // 4. 验数据库（状态应保持原样）
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.GENERATE_SERVICEORDER.byteValue(), updatedPo.getStatus());
    }

    /**
     * 场景4：售后单中没有服务单号ID -> 整体失败，抛出异常
     */
    @Test
    public void cancelAftersale_NoServiceOrderId() throws Exception {
        // 构造没有服务单号的售后单状态
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);
        AftersaleOrder bo = CloneFactory.copy(new AftersaleOrder(), po);
        bo.setType(2);                                    // 维修
        bo.setStatus(AftersaleOrder.GENERATE_SERVICEORDER); // 已生成服务单
        bo.setShopId(1L);                                 // 与 URI 中的 shopId 保持一致
        bo.setCustomerId(1001L);
        bo.setCustomerName("维修用户赵六");
        bo.setCustomerMobile("13966668888");
        bo.setServiceOrderId(null);                       // 设置服务单号为 null

        AftersaleOrderPo toSavePo = CloneFactory.copy(po, bo);
        aftersaleOrderDao.update(toSavePo);
        // 打桩远程调用，模拟服务模块的返回
        InternalReturnObject<ServiceFind> findRet = new InternalReturnObject<>();
        findRet.setErrno(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo());
        findRet.setErrmsg("获取服务单信息失败");

        Mockito.when(serviceOrderClient.getServiceOrder(
                eq(1L),
                eq(8888L), // 这里使用了一个假设的服务单号，因为实际的服务单号是 null
                anyString()
        )).thenReturn(findRet);

        // 发请求
        mockMvc.perform(
                        put("/aftersales/{id}", targetId)
                                .header("authorization", "Bearer test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errno").value(ReturnNo.RESOURCE_ID_NOTEXIST.getErrNo())) // 假设没有服务单号时返回的errno
                .andDo(print());

        // 验数据库（状态应保持原样）
        AftersaleOrderPo updatedPo = aftersaleOrderDao.findById(targetId);
        assertEquals(AftersaleOrder.GENERATE_SERVICEORDER.byteValue(), updatedPo.getStatus());
    }

}
