package com.etna.myapi;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@Log4j2
@EnableCaching
@EnableAsync
@SpringBootApplication
public class MyapiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyapiApplication.class, args);
    }

}
