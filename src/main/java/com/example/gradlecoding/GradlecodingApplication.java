package com.example.gradlecoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class GradlecodingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GradlecodingApplication.class, args);
	}

}
