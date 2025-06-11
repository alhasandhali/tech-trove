package com.spring.boot.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TechTroveApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechTroveApplication.class, args);
        System.out.println("Welcome to TechTrove Application");
        System.err.println("----------------------------------------------------");
    }
}
