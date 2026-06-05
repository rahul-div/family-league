package com.familyleague;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.familyleague.common.config.AppProperties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
public class FamilyLeagueApplication {

    public static void main(String[] args) {
        SpringApplication.run(FamilyLeagueApplication.class, args);
    }
}
