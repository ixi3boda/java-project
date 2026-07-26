package com.ejada.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Practice Spring Boot application.
 *
 * <p>This application exposes a RESTful API for an e-commerce backend,
 * including authentication (JWT), user management, product catalogue,
 * category management, and order processing.</p>
 *
 * <p>Run with {@code ./mvnw spring-boot:run} or package and execute the
 * resulting JAR.  All runtime configuration is supplied via environment
 * variables (see {@code .env} for local defaults).</p>
 */
@SpringBootApplication
public class PracticeApplication {

	/**
	 * Application entry point.
	 *
	 * @param args command-line arguments forwarded to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(PracticeApplication.class, args);
	}

}
