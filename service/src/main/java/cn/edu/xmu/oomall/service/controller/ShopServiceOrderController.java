package cn.edu.xmu.oomall.service.controller;

import cn.edu.xmu.javaee.core.aop.Audit;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.ServiceOrderService;
import cn.edu.xmu.oomall.service.service.vo.ServiceOrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 商铺服务单控制器
 */
@RestController
@RequestMapping(value = "/shops/{did}", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@Slf4j
public class ShopServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    /**
     * 商铺根据 id 查询服务单详情
     * @param did 店铺id
     * @param id 服务单id
     * @return 服务单详情 VO
     */
    @GetMapping("/services/{id}")
    // @Audit(departName = "shops")
    public ReturnObject getServiceOrderById(@PathVariable Long did, @PathVariable Long id) {
        log.debug("getServiceOrderById: did = {}, id = {}", did, id);
        ServiceOrder serviceOrder = this.serviceOrderService.findById(did, id);
        return new ReturnObject(new ServiceOrderVo(serviceOrder));
    }

}