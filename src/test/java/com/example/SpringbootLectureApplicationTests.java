package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootTest
@EnableAspectJAutoProxy
@EnableRetry
class SpringbootLectureApplicationTests {

    @Test
    void contextLoads() {
    }

}
