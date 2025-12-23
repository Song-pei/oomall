package cn.edu.xmu.oomall.service.service.strategy.impl.accept;

import cn.edu.xmu.javaee.core.model.InternalReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.ServiceProviderDao;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.service.feign.ExpressClient;
import cn.edu.xmu.oomall.service.service.feign.po.ExpressPo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ExpressAcceptActionTest {
    @Autowired
    private ExpressAcceptAction expressAcceptAction;

    @MockitoBean
    private ExpressClient expressClient;

    @MockitoBean
    private ServiceProviderDao serviceProviderDao;

    @Test
    public void testExecute_Success() {
        // 1. 准备数据
        ServiceOrder order = new ServiceOrder();
        order.setId(101L);
        order.setShopId(1L);
        order.setMaintainerId(200L);

        ServiceProvider mockProvider = new ServiceProvider();
        mockProvider.setRegionId(10L);
        mockProvider.setMobile("13812345678");

        // 2. 打桩 (Stubbing)
        Mockito.when(serviceProviderDao.findById(anyLong())).thenReturn(mockProvider);

        ExpressPo po = new ExpressPo(999L, "EX123456");
        InternalReturnObject<ExpressPo> ret = new InternalReturnObject<>(po);
        ret.setErrno(0);

        Mockito.when(expressClient.createPackage(anyLong(), any(), any(), any()))
                .thenReturn(ret);

        // 3. 执行测试
        UserToken user = new UserToken();
        Byte result = expressAcceptAction.execute(order, user);

        // 4. 断言
        assertEquals(999L, order.getExpressId());
    }
}
