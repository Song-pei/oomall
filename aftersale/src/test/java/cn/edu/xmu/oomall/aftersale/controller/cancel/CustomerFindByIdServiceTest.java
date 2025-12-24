package cn.edu.xmu.oomall.aftersale.controller.cancel;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.strategy.config.StrategyRouter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AftersaleOrderService 单元测试
 * 只测 Service 逻辑，DAO 与 CloneFactory 全部打桩
 */
@ExtendWith(MockitoExtension.class)
class CustomerFindByIdServiceTest {

    @Mock
    private AftersaleOrderDao aftersaleOrderDao;

    @Mock
    private StrategyRouter strategyRouter;   // 本方法未用到，但构造器需要，所以 mock 占位

    @InjectMocks
    private AftersaleOrderService aftersaleOrderService;

    /* ----------------------------------------------------------- */
    /*  正常场景：查到 PO 并转成 BO 返回                           */
    /* ----------------------------------------------------------- */
    @Test
    @DisplayName("customerSearch-成功")
    void customerSearch_ok() {
        // 1. 构造 PO
        AftersaleOrderPo po = new AftersaleOrderPo();
        po.setId(1L);
        po.setShopId(10L);
        po.setCustomerId(100L);
        po.setStatus(AftersaleOrder.UNAUDIT);
        // 2. 构造 BO（期望结果）
        AftersaleOrder expectedBo = CloneFactory.copy(new AftersaleOrder(), po);
        // 3. 打桩 DAO
        when(aftersaleOrderDao.findById(1L)).thenReturn(po);
        // 4. 因为 CloneFactory.copy 是静态方法，需要 mock static
        try (MockedStatic<CloneFactory> cf = mockStatic(CloneFactory.class)) {
            cf.when(() -> CloneFactory.copy(any(AftersaleOrder.class), eq(po)))
                    .thenReturn(expectedBo);
            // 5. 执行
            AftersaleOrder actualBo = aftersaleOrderService.customerSearch(1L, new UserToken());
            // 6. 断言
            assertThat(actualBo).isSameAs(expectedBo);
        }
    }

    /* ----------------------------------------------------------- */
    /*  异常场景：PO 不存在抛 BUSINESS_EXCEPTION                   */
    /* ----------------------------------------------------------- */
    @Test
    @DisplayName("customerSearch-资源不存在")
    void customerSearch_notFound() {
        // 1. 打桩 DAO 返回 null
        when(aftersaleOrderDao.findById(999L)).thenReturn(null);

        // 2. 执行 & 断言
        BusinessException ex = catchThrowableOfType(
                () -> aftersaleOrderService.customerSearch(999L, new UserToken()),
                BusinessException.class);

        assertThat(ex.getErrno()).isEqualTo(ReturnNo.RESOURCE_ID_NOTEXIST);
        assertThat(ex.getMessage()).contains("售后单不存在");
    }
}