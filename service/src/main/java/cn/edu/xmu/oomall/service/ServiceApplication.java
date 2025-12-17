package cn.edu.xmu.oomall.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 只保留 Nacos 的排除（如果你本地还是没装 Nacos）
// 去掉所有关于 Redis 的排除
@SpringBootApplication(
//        excludeName = {
//                "org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration",
//                "com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration",
//                "com.alibaba.cloud.nacos.NacosConfigAutoConfiguration"
//        },
        // 恢复原来的包扫描（或者直接删掉 scanBasePackages 使用默认扫描）
        scanBasePackages = {"cn.edu.xmu.javaee.core", "cn.edu.xmu.oomall.service"}
)
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}