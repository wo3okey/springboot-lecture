package com.example;

import com.example.service.CounterRedisService;
import com.example.service.CounterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StopWatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@SpringBootTest
public class CounterServiceTest {
    @Autowired
    private CounterService counterService;

    @Autowired
    private CounterRedisService counterRedisService;

    StopWatch stopWatch = new StopWatch();

    int initCount = 100;
    int failCount = 0;

    @BeforeEach
    void init() {
        counterService.init(initCount);
        stopWatch.start();
    }

    @AfterEach
    void result() {
        stopWatch.stop();
        printResult();
    }

    private void printResult() {
        int successCount = initCount - counterService.count();
        int failCount;
        if (this.failCount != 0) {
            failCount = this.failCount;
        } else {
            failCount = initCount - successCount;
        }

        System.out.println("### 성공 갯수: " + successCount);
        System.out.println("### 실패 횟수: " + failCount);
        System.out.println("### 동작시간: : " + stopWatch.getTotalTimeSeconds() + "초");
    }

    @Test
    public void 락_없이() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            counterService.decrease();
        });
    }

    @Test
    public void 락_없이_sync() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            counterService.decreaseSync();
        });
    }

    @Test // 실행전 version 주석 해제
    public void 낙관적락() {
        AtomicInteger failCount = new AtomicInteger();

        IntStream.range(0, 100).parallel().forEach(i -> {
            try {
                counterService.decrease();
            } catch (ObjectOptimisticLockingFailureException e) {
                failCount.getAndIncrement();
            }
        });
    }

    @Test // 실행전 version 주석 해제
    public void 낙관적락_retry() {
        int maxRetryCount = 5;
        AtomicInteger failCount = new AtomicInteger();

        IntStream.range(0, 100).parallel().forEach(i -> {
            int attempts = 0;
            while (attempts < maxRetryCount) {
                try {
                    counterService.decrease();
                    break;
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.getAndIncrement();
                    attempts++;
                }
            }
        });

        this.failCount = failCount.get();
    }

    @Test
    public void 비관적락() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            counterService.decreasePessimistic();
        });
    }

    @Test
    public void redis_스핀락() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            counterRedisService.redisSpinLock();
        });
    }

    @Test
    public void redisson_분산락() {
        IntStream.range(0, 100).parallel().forEach(i -> {
            counterRedisService.redissonLock();
        });
    }
}
