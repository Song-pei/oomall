package cn.edu.xmu.oomall.aftersale.service.strategy.config;


import cn.edu.xmu.oomall.aftersale.service.strategy.action.AuditAction;
import cn.edu.xmu.oomall.aftersale.service.strategy.action.CancelAction;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StrategyRouter {

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private StrategyProperties strategyProperties;

    // 两个 Map，分别存放 "审核策略" 和 "取消策略"
    private final Map<StrategyKey, AuditAction> auditMap = new HashMap<>();
    private final Map<StrategyKey, CancelAction> cancelMap = new HashMap<>();

    /**
     * 核心初始化逻辑
     * 项目启动时自动执行一次
     */
    @PostConstruct
    public void init() {
        log.info("======== 开始加载售后策略路由配置 ========");

        if (strategyProperties.getStrategies() == null || strategyProperties.getStrategies().isEmpty()) {
            log.warn("⚠ 未检测到策略配置，请检查 strategy-rules.yml 或 application.yml");
            return;
        }

        for (StrategyProperties.Rule rule : strategyProperties.getStrategies()) {
            String beanName = rule.getBeanName();

            // 1. 检查 Spring 容器里有没有这个名字的 Bean
            if (!applicationContext.containsBean(beanName)) {
                log.error(" 配置错误：找不到名为 [{}] 的 Bean，请检查类上是否有 @Component(\"{}\")", beanName, beanName);
                continue;
            }

            // 2. 构建 Key (Type + Status + Opt)
            StrategyKey key = new StrategyKey(rule.getType(), rule.getStatus(), rule.getOpt());

            // 3. 从 Spring 容器取出 Bean
            Object bean = applicationContext.getBean(beanName);

            // 4. 根据操作类型 (AUDIT / CANCEL) 分类放入 Map
            if ("AUDIT".equalsIgnoreCase(rule.getOpt())) {
                if (bean instanceof AuditAction) {
                    auditMap.put(key, (AuditAction) bean);
                    log.info(" 加载审核规则: {} -> {}", key, beanName);
                } else {
                    log.error(" 类型不匹配: Bean [{}] 没有实现 AuditAction 接口", beanName);
                }
            }
            else if ("CANCEL".equalsIgnoreCase(rule.getOpt())) {
                if (bean instanceof CancelAction) {
                    cancelMap.put(key, (CancelAction) bean);
                    log.info("加载取消规则: {} -> {}", key, beanName);
                } else {
                    log.error("类型不匹配: Bean [{}] 没有实现 CancelAction 接口", beanName);
                }
            }
        }
        log.info("======== 策略加载完毕，Audit:{}条, Cancel:{}条 ========", auditMap.size(), cancelMap.size());
    }

    /**
     * 对外提供的路由方法：获取审核策略
     */
    public AuditAction routeAudit(Integer type, Integer status) {
        return auditMap.get(new StrategyKey(type, status, "AUDIT"));
    }

    /**
     * 对外提供的路由方法：获取取消策略
     */
    public CancelAction routeCancel(Integer type, Integer status) {
        return cancelMap.get(new StrategyKey(type, status, "CANCEL"));
    }
}