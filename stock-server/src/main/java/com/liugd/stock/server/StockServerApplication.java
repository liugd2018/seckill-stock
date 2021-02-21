package com.liugd.stock.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.liugd.stock"})
@MapperScan("com.liugd.stock")
@SpringBootApplication
public class StockServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockServerApplication.class, args);
	}

}
