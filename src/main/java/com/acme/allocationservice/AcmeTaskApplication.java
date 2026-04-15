package com.acme.allocationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableResilientMethods
public class AcmeTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcmeTaskApplication.class, args);
    }

}
