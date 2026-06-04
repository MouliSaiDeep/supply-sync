package com.supplysync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SupplysyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupplysyncApplication.class, args);
	}

}
