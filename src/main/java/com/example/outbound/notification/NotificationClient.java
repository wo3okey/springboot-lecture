package com.example.outbound.notification;

public interface NotificationClient {

    void sendReleaseNotification(Long movieId, String movieName);

    void sendReleaseFailedNotification(Long movieId, String movieName);
}
