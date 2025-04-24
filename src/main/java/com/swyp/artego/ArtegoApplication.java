package com.swyp.artego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ArtegoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtegoApplication.class, args);
	}

}
