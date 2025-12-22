package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.oomall.aftersale.controller.dto.AuditAfterSalesDTO;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;
import cn.edu.xmu.oomall.aftersale.service.AftersaleOrderService;
import cn.edu.xmu.oomall.aftersale.service.vo.AftersaleOrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.edu.xmu.javaee.core.model.ReturnNo.*;

@RestController
@Transactional
@RequestMapping(produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final AftersaleOrderService aftersaleOrderService;
    /**
     *顾客取消某一售后单
     * @param id
     * @param user
     * @return
     */
    @DeleteMapping("aftersales/{id}")
    public ReturnObject customerCancel(
            @PathVariable Long id,
            UserToken user) {
        log.info("收到顾客取消售后单请求:  id={}, user={}",  id, user);

        // 如果没有登录（或者测试环境下），手动创建一个模拟的顾客
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));
            user = new UserToken();
            user.setId(1L);
            user.setName("customer-test");
            user.setDepartId(0L);
        }


        try {
            aftersaleOrderService.customerCancel(id, user);
            log.info("售后单取消成功: id={}", id);
            return new ReturnObject(ReturnNo.OK);
        } catch (IllegalArgumentException e) {
            log.warn("取消失败(参数错误): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.FIELD_NOTVALID, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("取消失败(状态不允许): id={}, error={}", id, e.getMessage());
            return new ReturnObject(ReturnNo.STATENOTALLOW, e.getMessage());
        } catch (Exception e) {
            log.error("取消售后单异常: id={}", id, e);
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }
}
