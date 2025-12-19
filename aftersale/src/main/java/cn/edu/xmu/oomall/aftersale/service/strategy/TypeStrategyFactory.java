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
        if (type == null) {
            return null;
        }
        return strategyMap.get(type.toString());
    }

}