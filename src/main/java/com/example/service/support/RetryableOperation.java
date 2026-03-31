package com.example.service.support;

import com.example.service.exception.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RetryableOperation {

    private static final Logger logger = LoggerFactory.getLogger(RetryableOperation.class);

    public <T> T executeWithRetry(
            int maxRetries,
            long delayMs,
            Supplier<T> operation,
            Supplier<T> fallback,
            String operationName
    ) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                logger.debug("[Retry] {} 시도 {}/{}", operationName, attempt, maxRetries);
                return operation.get();
            } catch (RetryableException e) {
                lastException = e;
                logger.warn("[Retry] {} 실패 (시도 {}/{}): {}",
                        operationName, attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    sleep(delayMs);
                }
            } catch (Exception e) {
                logger.error("[Retry] {} 재시도 불가능한 오류 발생", operationName, e);
                throw e;
            }
        }

        logger.warn("[Retry] {} 모든 재시도 실패 (총 {}회). Fallback 사용", operationName, maxRetries);
        return fallback.get();
    }

    public void executeWithRetryNoReturn(
            int maxRetries,
            long delayMs,
            Runnable operation,
            Runnable fallback,
            String operationName
    ) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                logger.debug("[Retry] {} 시도 {}/{}", operationName, attempt, maxRetries);
                operation.run();
                return;
            } catch (RetryableException e) {
                lastException = e;
                logger.warn("[Retry] {} 실패 (시도 {}/{}): {}",
                        operationName, attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    sleep(delayMs);
                }
            } catch (Exception e) {
                logger.error("[Retry] {} 재시도 불가능한 오류 발생", operationName, e);
                throw e;
            }
        }

        logger.warn("[Retry] {} 모든 재시도 실패 (총 {}회). Fallback 실행", operationName, maxRetries);
        fallback.run();
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", e);
        }
    }
}
