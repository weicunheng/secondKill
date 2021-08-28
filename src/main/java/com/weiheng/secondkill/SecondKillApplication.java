package com.weiheng.secondkill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("com.weiheng.secondkill")
@EnableScheduling
public class SecondKillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecondKillApplication.class, args);
    }
}
