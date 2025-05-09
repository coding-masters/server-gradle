package com.example.gradlecoding;

import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EntityScan(basePackages = "com.example.gradlecoding.domain")
@EnableJpaAuditing
public class GradlecodingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GradlecodingApplication.class, args);
	}

}
