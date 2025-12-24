package cn.edu.xmu.oomall.aftersale.controller.cancel;


import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.controller.CustomerController;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//@WebMvcTest(AftersaleOrderService.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ResourceOrStatusServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AftersaleOrderService aftersaleOrderService;
    @MockitoBean
    private AftersaleOrderDao aftersaleOrderDao;
    @MockitoBean
    private StrategyRouter strategyRouter;

    /**
     * 测试 service层售后单不存在场景
     */
    @Test
    public void testCustomerCancel_ResourceNotExist() throws Exception {

        when(aftersaleOrderDao.findById(anyLong())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            aftersaleOrderService.customerCancel(1L, new UserToken());
        });

        assert ex.getErrno() == ReturnNo.RESOURCE_ID_NOTEXIST;


    }


    /**
     * 测试状态 service层不允许取消场景
     */
    @Test
    public void testCustomerCancel_StateNotAllow() throws Exception {

        AftersaleOrderPo po = new AftersaleOrderPo();
        po.setId(1L);
        po.setStatus(AftersaleOrder.FINISH); // 不允许取消的状态

        when(aftersaleOrderDao.findById(anyLong())).thenReturn(po);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            aftersaleOrderService.customerCancel(1L, new UserToken());
        });

        assert ex.getErrno() == ReturnNo.STATENOTALLOW;
    }



}
