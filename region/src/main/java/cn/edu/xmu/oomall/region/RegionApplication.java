package cn.edu.xmu.oomall.region;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"cn.edu.xmu.javaee.core", "cn.edu.xmu.oomall.region"})
@EnableDiscoveryClient
public class RegionApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegionApplication.class, args);
    }
}
