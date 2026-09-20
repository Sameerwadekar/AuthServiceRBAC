package com.learn.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WorkFlowAuth {

	public static void main(String[] args) {
		SpringApplication.run(WorkFlowAuth.class, args);
	}
}
