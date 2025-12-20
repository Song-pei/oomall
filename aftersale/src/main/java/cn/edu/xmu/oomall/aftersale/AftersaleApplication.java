package cn.edu.xmu.oomall.aftersale;

import cn.edu.xmu.oomall.aftersale.service.strategy.TypeStrategyFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients(basePackages = "cn.edu.xmu.oomall.aftersale.service.feign")

@SpringBootApplication(
        scanBasePackages = {"cn.edu.xmu.javaee.core",
        "cn.edu.xmu.oomall.aftersale"})

public class AftersaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AftersaleApplication.class, args);
    }


    /**
     * 验证策略模式是否生效
     */
    @Bean
    public CommandLineRunner verifyStrategy(TypeStrategyFactory factory) {
        return args -> {
            System.out.println("\n========== 验证开始 ==========");

            // 你的验证逻辑
            var s2 = factory.getStrategy(2);
            var s3 = factory.getStrategy(3);

            System.out.println("S2: " + s2);
            System.out.println("S3: " + s3);
            System.out.println("是否同一对象: " + (s2 == s3));

            System.out.println("========== 验证结束 ==========\n");
        };
    }
}

