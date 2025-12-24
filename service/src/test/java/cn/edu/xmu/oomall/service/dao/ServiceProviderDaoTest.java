package cn.edu.xmu.oomall.service.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.service.dao.bo.ServiceProvider;
import cn.edu.xmu.oomall.service.mapper.ServiceProviderPoMapper;
import cn.edu.xmu.oomall.service.mapper.po.ServiceProviderPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
@DisplayName("服务商Dao层补充测试")
class ServiceProviderDaoTest {

    @Mock
    private ServiceProviderPoMapper serviceProviderPoMapper; // Mock 掉 Mapper 接口

    @InjectMocks
    private ServiceProviderDao serviceProviderDao; // 自动将 Mock 注入到 Dao 中

    @Test
    @DisplayName("数据库增加服务商")
    void testInsert() {
        // 1. 准备测试数据 (Given)
        ServiceProvider bo = new ServiceProvider();
        bo.setName("测试服务商");

        UserToken user = new UserToken();
        user.setId(123L);
        user.setName("Admin");
        user.setUserLevel(1);
        // 模拟 Mapper 的返回行为
        ServiceProviderPo savedPo = new ServiceProviderPo();
        savedPo.setId(1001L); // 模拟数据库生成的 ID
        savedPo.setName("测试服务商");

        when(serviceProviderPoMapper.save(any(ServiceProviderPo.class))).thenReturn(savedPo);

        // 2. 执行目标方法 (When)
        ServiceProvider result = serviceProviderDao.insert(bo, user);

        // 3. 验证结果 (Then)
        assertNotNull(result);
        assertEquals(1001L, result.getId()); // 验证 ID 是否成功回填
        assertEquals(user.getId(), result.getCreatorId()); // 验证创建者是否设置正确
        assertNotNull(result.getGmtCreate()); // 验证创建时间是否生成

        // 验证 Mapper 的 save 方法确实被调用了一次
        verify(serviceProviderPoMapper, times(1)).save(any(ServiceProviderPo.class));
    }
}