package cn.edu.xmu.oomall.aftersale.controller.cancel;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.strategy.impl.cancel.SimpleCancelAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleCancelActionControllerTest {
    @InjectMocks
    private SimpleCancelAction simpleCancelAction;

    /**
     *  待审核状态下的取消
     */
    @Test
    public void testExecute() {
        // 1. 模拟依赖
        AftersaleOrder mockAftersaleOrder = Mockito.mock(AftersaleOrder.class);

        Mockito.when(mockAftersaleOrder.getId()).thenReturn(123L);

        SimpleCancelAction action = new SimpleCancelAction();
        UserToken userToken = new UserToken();
        action.execute(mockAftersaleOrder,userToken);

        // 指定调用次数为 2 次
        Mockito.verify(mockAftersaleOrder, Mockito.times(2)).getId();

    }
}
