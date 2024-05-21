package com.unijorge.devsoft.AV3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Av3Application {

	public static void main(String[] args) {
		SpringApplication.run(Av3Application.class, args);
	}

}
