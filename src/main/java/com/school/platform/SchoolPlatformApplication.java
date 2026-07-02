package com.school.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.school.platform")
public class SchoolPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolPlatformApplication.class, args);
    }
}