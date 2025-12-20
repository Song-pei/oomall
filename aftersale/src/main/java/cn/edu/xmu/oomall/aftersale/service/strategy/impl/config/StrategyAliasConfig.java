package cn.edu.xmu.oomall.aftersale.service.strategy.impl.config;

import cn.edu.xmu.oomall.aftersale.service.strategy.impl.TypeStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyAliasConfig {

    /**
     * 1. 先把正主 "2" 注入进来
     * 注意：这里 TypeStrategy 是接口，必须用 @Qualifier 指定名字
     */
    @Autowired
    @Qualifier("2")
    private TypeStrategy fixStrategy2;

    /**
     * 2. 定义一个新 Bean 叫 "3"
     * 核心技巧：虽然它叫 "3"，但返回的是 "2" 的同一个实例引用
     * 效果：Spring 扫描时会认为 "3" 是一个正经的 Bean，所以会自动把它放入 Map 中
     */
    @Bean("3")
    public TypeStrategy fixStrategy3() {
        return fixStrategy2;
    }
}