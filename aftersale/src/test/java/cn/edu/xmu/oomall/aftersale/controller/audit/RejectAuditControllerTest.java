package cn.edu.xmu.oomall.aftersale.controller.audit;

import cn.edu.xmu.oomall.aftersale.AftersaleApplication;
import cn.edu.xmu.oomall.aftersale.dao.AftersaleOrderDao;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.mapper.po.AftersaleOrderPo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AftersaleApplication.class)
@AutoConfigureMockMvc
@Transactional // 保证测试完成后自动回滚，不污染数据库
public class RejectAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AftersaleOrderDao aftersaleOrderDao;

    @Test
    public void auditAftersale_Reject_Success() throws Exception {
        // 1. 准备数据：确保 ID=1 的售后单存在且为待审核状态 (0)
        Long targetId = 1L;

        // 2. 构造请求体 (confirm 为 false)
        String requestBody = """
            {
              "confirm": false,
              "conclusion": "不同意",
              "reason": "超过质保期"
            }
            """;

        // 3. 发起调用
        mockMvc.perform(put("/shops/1/aftersales/" + targetId + "/confirm")
                        .header("authorization", "no-need-token") // 由于没卡权限，传任意值或不传即可
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errno").value(0));

        // 4. 验证数据库状态更新
        AftersaleOrderPo po = aftersaleOrderDao.findById(targetId);
        assertEquals("不同意", po.getConclusion());
        assertEquals("超过质保期", po.getReason());
    }
}