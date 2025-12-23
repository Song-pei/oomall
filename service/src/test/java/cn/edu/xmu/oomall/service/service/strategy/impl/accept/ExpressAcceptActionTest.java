package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderAcceptDto;
import cn.edu.xmu.oomall.service.dao.ServiceProviderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import cn.edu.xmu.oomall.service.service.strategy.action.AcceptAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExpressAcceptActionTest {

    @Autowired
    private AcceptAction expressAcceptAction; // 看看这行会不会报错
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ExpressClient expressClient;
    @BeforeEach
    public void setup() {
        // 1. 清理数据
        jdbcTemplate.execute("DELETE FROM service_service WHERE id = 100");
        jdbcTemplate.execute("DELETE FROM service_provider WHERE id = 1");

        // 2. 插入服务商数据
        jdbcTemplate.execute("INSERT INTO service_provider " +
                "(id, name, consignee, address, mobile, region_id, status, gmt_create) " +
                "VALUES (1, '快捷服务商', '服务商联系人', '厦门市思明区', '13800000000', 350203, 1, NOW())");

        // 3. 插入服务单数据
        jdbcTemplate.execute("INSERT INTO service_service " +
                "(id, maintainer_id, shop_id, region_id, address, consignee, mobile, status, type,gmt_create) " +
                "VALUES (100, 1, 200, 350206, '软件园二期', '用户小张', '13911112222', 0, 1,NOW())");

        System.out.println("---- Database Setup Complete for service_service and service_provider ----");
    }
    @Test
    public void testAcceptServiceOrder_Success() throws Exception {
        // 模拟返回对象
        ExpressPo mockExpressPo = new ExpressPo();
        mockExpressPo.setId(888L);
        mockExpressPo.setBillCode("TestBillCode");
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(mockExpressPo);

        // 打桩 (Stubbing)
        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");

        mockMvc.perform(post("/maintainers/1/services/100/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(ReturnNo.OK.getErrNo()));
    }
    @Test
    public void testAcceptServiceOrder_RemoteFail() throws Exception {
        // 模拟远程调用返回错误码
        InternalReturnObject<ExpressPo> mockRet = new InternalReturnObject<>(ReturnNo.REMOTE_SERVICE_FAIL.getErrNo(), "物流模块异常");

        Mockito.when(expressClient.createPackage(any(), any(), any(), any()))
                .thenReturn(mockRet);

        ServiceOrderAcceptDto dto = new ServiceOrderAcceptDto();
        dto.setConfirm(true);
        dto.setMaintainername("测试员工");
        dto.setMaintainermobile("13812345678");
        mockMvc.perform(post("/maintainers/1/services/100/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // 你的 Controller 中 catch 了 Exception 并返回 INTERNAL_SERVER_ERR
                .andExpect(jsonPath("$.errno").value(ReturnNo.INTERNAL_SERVER_ERR.getErrNo()));
    }
}
