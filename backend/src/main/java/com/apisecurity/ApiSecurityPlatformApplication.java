package com.apisecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiSecurityPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiSecurityPlatformApplication.class, args);
    }
}
