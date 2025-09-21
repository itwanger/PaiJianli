package com.yizhaoqi.pairesume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.yizhaoqi.pairesume.repository")
public class PaiResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaiResumeApplication.class, args);
    }

}
