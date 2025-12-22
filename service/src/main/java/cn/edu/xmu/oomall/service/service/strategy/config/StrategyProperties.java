package cn.edu.xmu.oomall.service.service.strategy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ser-vice") // 对应 yaml 前缀
public class StrategyProperties {

    private List<Rule> strategies;

    @Data
    public static class Rule {
        private Integer type;
        private Integer status;
        private String opt;       // "AUDIT" 或 "CANCEL"..............
        private String beanName;  // Spring Bean 的名字
        private String desc;
    }
}