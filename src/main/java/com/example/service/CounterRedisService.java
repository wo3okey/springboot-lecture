package com.example.service;

import com.example.domain.entity.Counter;
import com.example.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CounterRedisService {
    private final CounterService counterService;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_KEY = "lock_key";
    private static final String LOCK_VALUE = "lock_value";
    private static final int LOCK_TIME_OUT = 3000;
    private static final int LOCK_WAIT_TIME = 5;
    private static final int LOCK_LEASE_TIME = 10;

    public Boolean lock() {
        return redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, LOCK_VALUE, Duration.ofMillis(LOCK_TIME_OUT));
    }

    public void unlock() {
        redisTemplate.delete(LOCK_KEY);
    }

    public void redisSpinLock() {
        while (!lock()) {
            try {
                Thread.sleep(LOCK_WAIT_TIME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            counterService.decrease();
        } finally {
            unlock();
        }
    }

    public void redissonLock() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    counterService.decrease();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
