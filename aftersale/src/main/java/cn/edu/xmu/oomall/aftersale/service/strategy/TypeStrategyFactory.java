package cn.edu.xmu.oomall.aftersale.service.strategy;

import cn.edu.xmu.oomall.aftersale.service.strategy.impl.TypeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TypeStrategyFactory {
    @Autowired
    private Map<String, TypeStrategy> strategyMap;

    public TypeStrategy getStrategy(Integer type) {
        // 注意：你的入参是 Integer，但 Map 的 Key 是 String
        // 所以这里要做一次 toString() 转换
        if (type == null) {
            return null;
        }
        return strategyMap.get(type.toString());
    }

}