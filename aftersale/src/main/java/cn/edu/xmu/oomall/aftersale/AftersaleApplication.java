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
}

