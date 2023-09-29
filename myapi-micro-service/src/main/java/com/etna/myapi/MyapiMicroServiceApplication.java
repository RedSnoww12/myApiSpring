package com.etna.myapi;

import lombok.Generated;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@Log4j2
@EnableCaching
@Generated
@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.etna.myapi", "com.etna.myapi.api", "com.etna.myapi.controller", "com.etna.myapi.services",
        "com.etna.myapi.config", "com.etna.myapi.dataobjects", "com.etna.myapi.dataobjects.mappers", "com.etna.myapi.entity"})
public class MyapiMicroServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyapiMicroServiceApplication.class, args);
    }

}
