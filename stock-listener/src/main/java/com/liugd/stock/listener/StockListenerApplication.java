package com.liugd.stock.listener;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@ComponentScan(basePackages = {"com.liugd.stock"})
@MapperScan("com.liugd.stock")
@SpringBootApplication
public class StockListenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockListenerApplication.class, args);
	}

}
