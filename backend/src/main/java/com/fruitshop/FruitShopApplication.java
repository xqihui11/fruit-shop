package com.fruitshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.fruitshop.mapper")
@EnableScheduling
public class FruitShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(FruitShopApplication.class, args);
    }
}

