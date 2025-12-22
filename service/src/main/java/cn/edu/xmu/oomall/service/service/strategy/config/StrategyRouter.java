package cn.edu.xmu.oomall.service.service.strategy.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * 售后策略路由器
 */

@Slf4j
@Component
public class StrategyRouter {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private StrategyProperties strategyProperties;

    // 一个通用的 Map，Value 存 Object
    private final Map<StrategyKey, Object> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("======== 开始加载服务策略路由配置 ========");

        if (strategyProperties.getStrategies() == null) return;

        for (StrategyProperties.Rule rule : strategyProperties.getStrategies()) {
            String beanName = rule.getBeanName();

            if (!applicationContext.containsBean(beanName)) {
                log.error(" 配置错误：找不到 Bean [{}]", beanName);
                continue;
            }

            // 直接构建 Key -> Bean 的映射
            StrategyKey key = new StrategyKey(rule.getType(), rule.getStatus(), rule.getOpt());
            Object bean = applicationContext.getBean(beanName);

            strategyMap.put(key, bean);
            log.info("加载策略: {} -> {}", key, beanName);
        }
        log.info("======== 策略加载完毕，共 {} 条 ========", strategyMap.size());
    }

    /**
     * 路由方法
     * 利用泛型 <T>
     * * @param type 订单类型
     * @param status 订单状态
     * @param opt 操作类型 ("AUDIT", "CANCEL", 等)
     * @param requiredType 期望返回的接口类型 (AuditAction.class 等)
     * @return 具体策略实现
     */
    public <T> T route(Byte type, Byte status, String opt, Class<T> requiredType) {
        StrategyKey key = new StrategyKey(type, status, opt);
        Object bean = strategyMap.get(key);

        // 检查是否存在
        if (bean == null) {
            return null;
        }

        // 检查类型是否匹配 (比如配置里 opt写了AUDIT，但对应的Bean没实现AuditAction)
        if (requiredType.isInstance(bean)) {
            return requiredType.cast(bean); // 安全强转
        } else {
            log.error(" 策略类型不匹配! Key={} 对应的Bean是 {}, 但期望是 {}", key, bean.getClass().getSimpleName(), requiredType.getSimpleName());
            return null;
        }
    }
}