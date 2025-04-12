package com.live_commerce.livebroadcast.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;


public interface BroadcastProductQueryRepository{

    Page<UUID> findProductIdsByBroadcastId(UUID broadcastId, Pageable pageable);
}
