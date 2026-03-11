package com.college.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmBackendApplication.class, args);
	}

}
