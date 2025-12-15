//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Ming Qiu
 */
@SpringBootApplication(scanBasePackages = {"cn.edu.xmu.javaee.core",
        "cn.edu.xmu.oomall.product"})
@EnableMongoRepositories(basePackages = "cn.edu.xmu.oomall.product.infrastructure.mongo")
@EnableFeignClients
@EnableDiscoveryClient
public class ProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}

}
