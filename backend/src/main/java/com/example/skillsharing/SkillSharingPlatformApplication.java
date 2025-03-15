package com.example.skillsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.skillsharing.config.AppConfig;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(AppConfig.class)
public class SkillSharingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSharingPlatformApplication.class, args);
    }
}
