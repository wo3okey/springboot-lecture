package com.example.outbound.notification;

import com.example.service.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MockNotificationClient implements NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(MockNotificationClient.class);
    private final Random random = new Random();

    @Override
    public void sendReleaseNotification(Long movieId, String movieName) {
        logger.info("[MockNotificationClient] 개봉 알림 발송 요청: movieId={}, movieName={}", movieId, movieName);

        if (random.nextInt(100) < 25) {
            logger.warn("[MockNotificationClient] 알림 발송 실패 시뮬레이션: movieId={}", movieId);
            throw new ExternalServiceException("NotificationService", "Failed to send notification");
        }

        simulateLatency();

        logger.info("[MockNotificationClient] 개봉 알림 발송 완료: movieId={}, 대상자 수={}",
                movieId, random.nextInt(1000) + 100);
    }

    @Override
    public void sendReleaseFailedNotification(Long movieId, String movieName) {
        logger.info("[MockNotificationClient] 개봉 실패 알림 발송: movieId={}, movieName={}", movieId, movieName);

        simulateLatency();

        logger.info("[MockNotificationClient] 개봉 실패 알림 발송 완료: movieId={}", movieId);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(30 + random.nextInt(70));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
