package com.live_commerce.livebroadcast.application.validation;


import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiveBroadcastValidator {

    private final LiveBroadcastRepository liveBroadcastRepository;
    private final BroadcastProductRepository broadcastProductRepository;


    /**
     * 존재하는 방송인지 검증하고 반환
     */
    public LiveBroadcast validateExists(UUID broadcastId) {
        return liveBroadcastRepository.findByIdAndDeletedStatusFalse(broadcastId)
                .orElseThrow(LiveBroadcastException::forLiveBroadcastNotFound);
    }

    /**
     * 방송에 이미 연결된 상품인지 검증 (연결되어 있으면 예외 발생)
     */
    public void validateNotConnected(UUID broadcastId, UUID productId) {
        if (broadcastProductRepository.existsByBroadcastIdAndProductIdAndDeletedStatusFalse(broadcastId, productId)) {
            throw LiveBroadcastException.forProductAlreadyConnected();
        }
    }

    /**
     * 방송에 연결된 상품이 존재하는지 검증하고 반환 (없으면 예외 발생)
     */
    public BroadcastProduct validateConnectedProductExists(UUID broadcastId, UUID productId) {
        return broadcastProductRepository.findByBroadcastIdAndProductIdAndDeletedStatusFalse(broadcastId, productId)
                .orElseThrow(LiveBroadcastException::forConnectedProductNotFound);

    }

}
