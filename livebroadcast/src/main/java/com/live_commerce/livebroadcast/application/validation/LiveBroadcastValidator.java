package com.live_commerce.livebroadcast.application.validation;


import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastUpdateRequestDto;
import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        return liveBroadcastRepository.findByLiveBroadcastIdAndDeletedStatusFalse(broadcastId)
                .orElseThrow(LiveBroadcastException::forLiveBroadcastNotFound);
    }

    /**
     * 방송에 이미 연결된 상품인지 검증 (연결되어 있으면 예외 발생)
     */
    public void validateNotConnected(UUID broadcastId, UUID productId) {
        if (broadcastProductRepository.existsByLiveBroadcastIdAndProductIdAndDeletedStatusFalse(broadcastId, productId)) {
            throw LiveBroadcastException.forProductAlreadyConnected();
        }
    }

    /**
     * 방송에 연결된 상품이 존재하는지 검증하고 반환 (없으면 예외 발생)
     */
    public BroadcastProduct validateConnectedProductExists(UUID broadcastId, UUID productId) {
        return broadcastProductRepository.findByLiveBroadcastIdAndProductIdAndDeletedStatusFalse(broadcastId, productId)
                .orElseThrow(LiveBroadcastException::forConnectedProductNotFound);

    }

    /**
     * 라이브 방송 생성 시, 시작 시간과 종료 시간의 유효성을 검증합니다.
     */
    public void validateCreatableRequest(LiveBroadcastCreateRequestDto dto) {
        if (!dto.startTime().isBefore(dto.endTime())) {
            throw LiveBroadcastException.forInvalidTimeRange();
        }
    }

    /**
     * 라이브 방송 수정 요청에 대한 유효성 검증을 수행합니다.
     */
    public void validateUpdateRequest(LiveBroadcastUpdateRequestDto dto, LiveBroadcast current) {

        boolean allNullOrBlank =
                (dto.broadcastName() == null || dto.broadcastName().isBlank()) &&
                        dto.startTime() == null &&
                        dto.endTime() == null &&
                        dto.broadcastStatus() == null;

        if (allNullOrBlank) {
            throw LiveBroadcastException.forUpdateFieldRequired();
        }

        LocalDateTime start = dto.startTime() != null ? dto.startTime() : current.getStartTime();
        LocalDateTime end = dto.endTime() != null ? dto.endTime() : current.getEndTime();

        if (!start.isBefore(end)) {
            throw LiveBroadcastException.forInvalidTimeRange();
        }
    }

}
