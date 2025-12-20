package cn.edu.xmu.oomall.aftersale.service.strategy;

import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TypeStrategyFactory {


    @Autowired
    private Map<String, TypeStrategy> strategyMap;

    public TypeStrategy getStrategy(Integer type) {
        if (type == null) {
            throw new BusinessException(ReturnNo.FIELD_NOTVALID, "售后类型不能为空");
        }

        // 使用 type.toString() 作为 Key 获取
        TypeStrategy strategy = strategyMap.get(type.toString());

        if (strategy == null) {

            throw new BusinessException(ReturnNo.FIELD_NOTVALID, "未找到对应的售后策略: type=" + type);
        }
        return strategy;
    }
}