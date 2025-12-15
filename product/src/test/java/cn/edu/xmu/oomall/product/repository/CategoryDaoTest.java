package cn.edu.xmu.oomall.product.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.mapper.RedisUtil;
import cn.edu.xmu.oomall.product.ProductTestApplication;
import cn.edu.xmu.oomall.product.model.Category;

@SpringBootTest(classes = ProductTestApplication.class)

public class CategoryDaoTest {
    /**
     * @author huang zhong
     * @task 2023-dgn2-005
     */
    @Autowired
    CategoryRepository categoryRepository;
    @MockBean
    private RedisUtil redisUtil;

    @Test
    public void testFindNoOnsaleByIdWhenNull() {
        Category category = new Category();
        category.setName("test");
        Mockito.when(redisUtil.hasKey("C1")).thenReturn(true);
        Mockito.when(redisUtil.get("C1")).thenReturn(category);
        assertEquals(category.getName(), categoryRepository.findById(1l).getName());
    }

    /**
     * @author huang zhong
     * @task 2023-dgn2-005
     */
    @Test
    public void testFindNoOnsaleByIdWhenNotExist() {
        assertThrows(BusinessException.class, () -> categoryRepository.findById(100000l));
    }

    /**
     * @author huangzian
     *         2023-dgn2-004
     */
    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testDelete() {
        assertThrows(BusinessException.class, () -> categoryRepository.delete(500L));
    }

}
