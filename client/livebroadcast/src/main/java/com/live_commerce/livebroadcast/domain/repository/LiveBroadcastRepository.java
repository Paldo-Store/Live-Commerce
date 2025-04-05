package com.live_commerce.livebroadcast.domain.repository;

import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface LiveBroadcastRepository {
    <S extends LiveBroadcast> S save(S liveBroadcast);
    // Optional<LiveBroadcast> findById(UUID id);
}
