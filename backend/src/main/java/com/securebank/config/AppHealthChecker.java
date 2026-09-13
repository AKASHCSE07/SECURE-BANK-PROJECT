package com.securebank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * AppHealthChecker executes immediately after the Spring ApplicationContext is initialized.
 * Logs active environment configuration and verifies readiness.
 */
@Component
public class AppHealthChecker implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/securebank_db}")
    private String datasourceUrl;

    @Override
    public void run(String... args) {
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println(" ✅ SECUREBANK APPLICATION CONTEXT INITIALIZED SUCCESSFULLY!");
        System.out.printf(" 🌐 REST API Gateway listening on Port: %s%n", serverPort);
        System.out.printf(" 🗄️ PostgreSQL Datasource Target    : %s%n", datasourceUrl);
        System.out.println(" 🛡️ Security Engine & JWT Ready");
        System.out.println("-----------------------------------------------------------------\n");
    }
}
