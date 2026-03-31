package com.example.domain.enums;

public enum ReleaseFailureReason {
    INVALID_ID("유효하지 않은 영화 ID"),
    NOT_FOUND("영화를 찾을 수 없음"),
    ALREADY_RELEASED("이미 개봉된 영화"),
    SHOWING_ENDED("상영 종료된 영화"),
    EXTERNAL_SERVICE_ERROR("외부 서비스 오류"),
    CRITICAL_ERROR("치명적 오류"),
    UNKNOWN_ERROR("알 수 없는 오류");

    private final String description;

    ReleaseFailureReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
