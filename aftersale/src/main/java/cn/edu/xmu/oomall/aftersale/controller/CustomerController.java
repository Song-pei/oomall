package cn.edu.xmu.oomall.aftersale.controller;

import cn.edu.xmu.javaee.core.exception.BusinessException;
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
     * 顾客根据id查询售后单信息
     */
    @GetMapping("/aftersales/{id}")
    public ReturnObject customerSearch(
            @PathVariable Long id,UserToken user){
        log.info("收到顾客查询售后单请求:  id={}, user={}",  id, user);

        // 如果没有登录（测试环境下），手动创建一个模拟的顾客
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));
            user = new UserToken();
            user.setId(1L);
            user.setName("customer-test");
            user.setDepartId(0L);
            user.setUserLevel(0);
        }
        try {

            AftersaleOrder bo=aftersaleOrderService.customerSearch(id, user);
            log.info("售后单查询成功: id={}", id);
            AftersaleOrderVo vo = CloneFactory.copy(new AftersaleOrderVo(), bo);
            return new ReturnObject(vo);

        }catch (BusinessException e){

            // 资源不存在
            if (e.getErrno() == ReturnNo.RESOURCE_ID_NOTEXIST) {
                log.warn("取消失败(资源不存在): id={}", id);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            // 其余 BusinessException（远程失败等）继续抛出 -> HTTP 500
            throw e;
        }

    }
    /**
     *顾客取消某一售后单
     * @param id
     * @param user
     * @return
     */
    @PutMapping("/aftersales/{id}")
    public ReturnObject customerCancel(
            @PathVariable Long id,
            UserToken user) {
        log.info("收到顾客取消售后单请求:  id={}, user={}",  id, user);

        // 如果没有登录（测试环境下），手动创建一个模拟的顾客
        if (user == null || user.getId() == null || user.getName() == null) {
            log.warn("检测到用户信不完整 (id={}, name={})，启用 Mock 用户",
                    (user != null ? user.getId() : "null"),
                    (user != null ? user.getName() : "null"));
            user = new UserToken();
            user.setId(1L);
            user.setName("customer-test");
            user.setDepartId(0L);
            user.setUserLevel(0);
        }


        try {

             aftersaleOrderService.customerCancel(id, user);
            log.info("售后单取消成功: id={}", id);
            return new ReturnObject(ReturnNo.OK);

        } catch (BusinessException be) {
            // 资源不存在
            if (be.getErrno() == ReturnNo.RESOURCE_ID_NOTEXIST) {
                log.warn("取消失败(资源不存在): id={}", id);
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            // 状态不允许
            if (be.getErrno()== ReturnNo.STATENOTALLOW) {
                log.warn("取消失败(状态不允许): id={}", id);
                return new ReturnObject(ReturnNo.STATENOTALLOW);
            }
            // 其余 BusinessException（远程失败等）继续抛出 -> HTTP 500
            throw be;
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("取消失败(业务校验): id={}, error={}", id, e.getMessage());
            return new ReturnObject(RESOURCE_ID_NOTEXIST, e.getMessage());
        }
    }
}
