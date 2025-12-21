package cn.edu.xmu.oomall.aftersale.service.strategy.config;

/**
 * 策略路由的“组合键”
 * 用于 Map 的 Key，必须重写 equals 和 hashCode
 * Java 21 record 自动完成了这些工作
 */
public record StrategyKey(Integer type, Integer status, String opt) {
}