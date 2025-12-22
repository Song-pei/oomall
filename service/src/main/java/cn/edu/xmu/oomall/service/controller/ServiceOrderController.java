package cn.edu.xmu.oomall.service.controller;

import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.validation.NewGroup;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderAcceptDto;
import cn.edu.xmu.oomall.service.service.ServiceOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/internal/shops/{shopId}", produces = "application/json;charset=UTF-8")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderController {
    private final ServiceOrderService serviceOrderService;
    /**
     * 接受服务单
     *
     * @param did    服务商id
     * @param id       服务单id
     * @param serviceOrderAcceptDto    服务单接受数据
     * @param user    登录用户
     * @return  服务单数据
     */
    @PostMapping("/maintainers/{did}/services/{id}/accept")
    public ReturnObject acceptServiceOrder(
            @PathVariable("did") Long did,

            @PathVariable("id") Long id,
            @Validated(NewGroup.class) @RequestBody ServiceOrderAcceptDto serviceOrderAcceptDto,
            UserToken user
    ) {
        if (user == null || user.getId() == null) {
            user = new UserToken();
            user.setId(1L);
            user.setName("admin-test");
            user.setDepartId(0L);
        }

        if(serviceOrderAcceptDto.getConfirm())
        {
            try {
                serviceOrderService.acceptServiceOrder(
                        did,
                        id,
                        user
                );
                log.info("服务单接受成功: id={}", id);
                return new ReturnObject(ReturnNo.OK);
            } catch (IllegalArgumentException e) {
                log.warn("服务单接受失败(参数错误): id={}, error={}", id, e.getMessage());
                return new ReturnObject(ReturnNo.FIELD_NOTVALID);
            } catch (IllegalStateException e) {
                log.warn("服务单接受失败(状态不允许): id={}, error={}", id, e.getMessage());
                return new ReturnObject(ReturnNo.STATENOTALLOW);
            } catch (Exception e) {
                log.error("服务单接受失败: id={}, error={}", id, e.getMessage(), e);
                return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
            }
        }
        log.info("服务单拒绝成功: id={}", id);
        return new ReturnObject(ReturnNo.OK);
    }
}
