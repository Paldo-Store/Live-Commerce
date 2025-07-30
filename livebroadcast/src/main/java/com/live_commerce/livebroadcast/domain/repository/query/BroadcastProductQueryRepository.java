package com.live_commerce.livebroadcast.domain.repository.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface BroadcastProductQueryRepository{

    Page<UUID> findProductIdsByBroadcastId(UUID broadcastId, Pageable pageable);
}
