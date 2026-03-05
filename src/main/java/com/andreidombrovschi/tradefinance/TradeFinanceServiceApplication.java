package com.andreidombrovschi.tradefinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeFinanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeFinanceServiceApplication.class, args);
	}

}
