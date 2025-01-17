package com.dzieger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.dzieger")
public class PlayerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlayerServiceApplication.class, args);
	}

}
