package cn.edu.xmu.oomall.aftersale.service.strategy;

import cn.edu.xmu.oomall.aftersale.service.strategy.impl.AuditStrategy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AuditStrategyFactory implements ApplicationContextAware {

    private static final List<AuditStrategy> STRATEGY_REGISTRY = new ArrayList<>();

    /**
     * Spring 容器启动生命周期回调方法。
     * <p>
     * 机制说明：
     * 1. 当 Spring 容器启动并加载完所有 Bean 后，会自动调用此方法。
     * 2. applicationContext.getBeansOfType 能够利用反射机制，自动扫描容器中所有实现了 AuditStrategy 接口的组件（如 FixStrategy）。
     * 3. 将扫描到的策略实现类自动注册到静态列表 STRATEGY_REGISTRY 中，实现策略的“自动发现”与“自动装配”。
     * * @param applicationContext Spring 应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AuditStrategy> beans = applicationContext.getBeansOfType(AuditStrategy.class);
        STRATEGY_REGISTRY.clear();
        STRATEGY_REGISTRY.addAll(beans.values());
    }

    public static AuditStrategy getStrategy(Integer type, String conclusion) {
        for (AuditStrategy strategy : STRATEGY_REGISTRY) {
            // 循环询问每一个策略是否匹配
            if (strategy.match(type, conclusion)) {
                return strategy;
            }
        }
        return null;
    }
}