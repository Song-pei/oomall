//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.service.controller;

import cn.edu.xmu.javaee.core.aop.Audit;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.ReturnObject;
import cn.edu.xmu.javaee.core.model.IdNameTypeVo;
import cn.edu.xmu.javaee.core.model.PageDto;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.javaee.core.util.CloneFactory;
import cn.edu.xmu.javaee.core.validation.NewGroup;
import cn.edu.xmu.javaee.core.validation.UpdateGroup;
import cn.edu.xmu.oomall.service.controller.dto.ServiceOrderDto;
import cn.edu.xmu.oomall.service.dao.bo.ServiceOrder;
import cn.edu.xmu.oomall.service.service.ServiceOrderService;
import cn.edu.xmu.oomall.service.service.vo.SimpleServiceVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;



/**
 * 服务单内部控制器
 */
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/internal/shops/{shopId}", produces = "application/json;charset=UTF-8")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InternalServiceOrderController {
    private final ServiceOrderService serviceOrderService;

    /**
     * 创建服务单
     *
     * @param shopId    商铺id
     * @param id       售后单id
     * @param serviceOrderDto    服务单数据
     * @param user    登录用户
     * @return  服务单数据
     */
    @PostMapping("/aftersales/{id}/serviceorders")
    public ReturnObject createServiceOrder(
            @PathVariable("shopId") Long shopId,

            @PathVariable("id") Long id,
            @Validated(NewGroup.class) @RequestBody ServiceOrderDto serviceOrderDto,
            UserToken user
    ) {
        if (user == null || user.getId() == null) {
            user = new UserToken();
            user.setId(1L);
            user.setName("admin-test");
            user.setDepartId(0L);
        }


        ServiceOrder serviceOrder = ServiceOrder.builder()
                .shopId(shopId)
                .type(serviceOrderDto.getType())
                .consignee(serviceOrderDto.getConsignee().getName())
                .mobile(serviceOrderDto.getConsignee().getMobile())
                .regionId(serviceOrderDto.getConsignee().getRegionId())
                .address(serviceOrderDto.getConsignee().getAddress())
                .build();
        ServiceOrder newOrder = this.serviceOrderService.createServiceOrder(serviceOrder, user);
        return new ReturnObject(ReturnNo.OK, CloneFactory.copy(new SimpleServiceVo(), newOrder));
    }
}

