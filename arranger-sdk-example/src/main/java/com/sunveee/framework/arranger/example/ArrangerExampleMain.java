package com.sunveee.framework.arranger.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.sunveee.framework.arranger" })
public class ArrangerExampleMain {

    public static void main(String[] args) {
        SpringApplication.run(ArrangerExampleMain.class, args);
    }
}
