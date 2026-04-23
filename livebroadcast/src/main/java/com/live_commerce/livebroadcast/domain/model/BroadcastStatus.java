package com.live_commerce.livebroadcast.domain.model;

public enum BroadcastStatus {
    SCHEDULED("방송 예정"),
    LIVE("방송 중"),
    ENDED("방송 종료");

    private final String description;

    BroadcastStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
