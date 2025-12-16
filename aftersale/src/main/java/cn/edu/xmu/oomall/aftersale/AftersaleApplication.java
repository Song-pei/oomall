package cn.edu.xmu.oomall.aftersale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "cn.edu.xmu.oomall.aftersale.service.feign")
@SpringBootApplication
public class AftersaleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AftersaleApplication.class, args);
    }
}

