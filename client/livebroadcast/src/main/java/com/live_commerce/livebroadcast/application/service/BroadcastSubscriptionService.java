package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.request.CreateSubscriptionRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.SubscriptionResponseDto;
import com.live_commerce.livebroadcast.application.mapper.SubscriptionMapper;
import com.live_commerce.livebroadcast.application.validation.SubscriptionValidator;
import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BroadcastSubscriptionService {

    private final BroadcastSubscriptionRepository subscriptionRepository;
    private final SubscriptionValidator subscriptionValidator;

    @Transactional
    public SubscriptionResponseDto subscribe(UUID userId, UUID broadcastId) {
        subscriptionValidator.validateNotSubscribed(userId, broadcastId);

        BroadcastSubscription subscription = BroadcastSubscription.create(userId, broadcastId);
        subscriptionRepository.save(subscription);

        // TODO : 알림 등록 api 호출 - x

        return SubscriptionMapper.toResponse(subscription);
    }

    @Transactional
    public void unsubscribe(UUID userId, UUID broadcastId) {
        BroadcastSubscription subscription = subscriptionValidator.getSubscriptionOrThrow(userId, broadcastId);

        // TODO : 알림 해제 호출

        subscription.delete(userId);
    }

    // 사용자의 구독 목록 조회(페이징만 처리)
    @Transactional(readOnly = true)
    public List<BroadcastSubscription> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findAllByUserIdAndDeletedStatusFalse(userId);
    }

    // 특정 방송의 유저 id 목록(페이징, 구독일순 정렬...?) 알림서비스에 보낼 용도...? 기둘
    @Transactional(readOnly = true)
    public List<UUID> getSubscriberUserIds(UUID broadcastId) {
        return subscriptionRepository.findAllByBroadcastIdAndDeletedStatusFalse(broadcastId)
                .stream()
                .map(BroadcastSubscription::getUserId)
                .toList();

        // 이거슨
    }


}
