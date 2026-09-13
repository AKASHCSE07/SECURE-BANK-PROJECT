package com.securebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for SecureBank Spring Boot Application.
 *
 * @SpringBootApplication encapsulates:
 * 1. @Configuration: Defines this class as a configuration source for Spring beans.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to automatically configure Spring beans based on pom.xml dependencies.
 * 3. @ComponentScan: Automatically scans and registers all @Component, @Service, @Repository, and @RestController classes in com.securebank.
 */
@SpringBootApplication
public class SecureBankApplication {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("            BOOTSTRAPPING SECUREBANK ENTERPRISE ENGINE           ");
        System.out.println("=================================================================");
        SpringApplication.run(SecureBankApplication.class, args);
    }
}
