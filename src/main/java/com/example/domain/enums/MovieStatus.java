package com.example.domain.enums;

public enum MovieStatus {
    PRE_RELEASE("개봉 전"),
    RELEASED("개봉 중"),
    END_OF_SHOWING("상영 종료");

    private final String description;

    MovieStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canRelease() {
        return this == PRE_RELEASE;
    }

    public boolean isReleased() {
        return this == RELEASED;
    }
}
