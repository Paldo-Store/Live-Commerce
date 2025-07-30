package com.live_commerce.livebroadcast.application.scheduler;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;



@Slf4j
@Component
@RequiredArgsConstructor
public class LiveBroadcastStatusScheduler {

    private final LiveBroadcastRepository liveBroadcastRepository;

    // 매 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void updateBroadcastStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<LiveBroadcast> candidates =
                liveBroadcastRepository.findAllByDeletedStatusFalseAndBroadcastStatusIn(
                        List.of(BroadcastStatus.SCHEDULED, BroadcastStatus.LIVE)
                );

        for (LiveBroadcast broadcast : candidates) {
            boolean updated = false;

            if (broadcast.getBroadcastStatus() == BroadcastStatus.SCHEDULED
                    && broadcast.getStartTime() != null
                    && !broadcast.getStartTime().isAfter(now)) {
                broadcast.updateStatus(BroadcastStatus.LIVE);
                updated = true;
            }

            if (broadcast.getBroadcastStatus() == BroadcastStatus.LIVE
                    && broadcast.getEndTime() != null
                    && !broadcast.getEndTime().isAfter(now)) {
                broadcast.updateStatus(BroadcastStatus.ENDED);
                updated = true;
            }

            if (updated) {
                log.info("방송 상태 전이됨: 방송ID={}, 새 상태={}", broadcast.getLiveBroadcastId(), broadcast.getBroadcastStatus());
            }
        }
    }
}
