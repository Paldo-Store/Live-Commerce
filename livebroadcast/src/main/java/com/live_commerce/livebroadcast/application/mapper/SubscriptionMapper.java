package com.live_commerce.livebroadcast.application.mapper;

import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;

import java.util.List;
import java.util.UUID;

public class SubscriptionMapper {

    public static SubscriptionResponseDto toResponse(BroadcastSubscription entity) {
        return new SubscriptionResponseDto(
                entity.getBroadcastId(),
                entity.getUserId(),
                entity.getBroadcastId(),
                entity.getCreatedAt()
        );
    }

    public static List<UUID> toUserIdList(List<BroadcastSubscription> subscriptionList) {
        return subscriptionList.stream()
                .map(BroadcastSubscription::getUserId)
                .toList();
    }


}
