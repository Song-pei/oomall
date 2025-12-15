package cn.edu.xmu.oomall.product.bo;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.product.ProductTestApplication;
import cn.edu.xmu.oomall.product.repository.GrouponActRepositoryTest;
import cn.edu.xmu.oomall.product.model.GrouponAct;
import cn.edu.xmu.oomall.product.model.OnSale;
import cn.edu.xmu.oomall.product.model.Product;
import cn.edu.xmu.oomall.product.repository.onsale.OnSaleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static cn.edu.xmu.javaee.core.model.Constants.MAX_RETURN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author WuTong
 * @task 2023-dgn2-008
 */
@SpringBootTest(classes = ProductTestApplication.class)
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class GrouponActTest {
    private final Logger logger = LoggerFactory.getLogger(GrouponActRepositoryTest.class);
    @MockBean
    private static OnSaleRepository onsaleRepository;
    @Test
    public void testInsertProductWithWrongAct() {


        OnSale onsale = new OnSale();
        onsale.setBeginTime(LocalDateTime.now());
        onsale.setEndTime(onsale.getBeginTime());
        List<OnSale> onSales = List.of(onsale);

        Product product = Mockito.mock(Product.class);
        GrouponAct grouponAct = new GrouponAct();
        grouponAct.setOnsaleRepository(onsaleRepository);
        grouponAct.setId(666L);

        Mockito.when(product.createOnsale(Mockito.any(OnSale.class), Mockito.any(UserToken.class)))
                .thenReturn(onsale);
        Mockito.when(onsaleRepository.retrieveByActId(666L, 1, MAX_RETURN)).thenReturn(onSales);

        try {
            grouponAct.addOnsaleOnAct(product,new OnSale(), new UserToken());
        } catch (BusinessException e){
            assertEquals(ReturnNo.STATENOTALLOW.getErrNo(), e.getErrno().getErrNo());
        }

    }

}
