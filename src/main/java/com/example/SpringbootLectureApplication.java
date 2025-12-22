package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Outbox Polling을 위한 스케줄링 활성화
public class SpringbootLectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootLectureApplication.class, args);
    }
}
