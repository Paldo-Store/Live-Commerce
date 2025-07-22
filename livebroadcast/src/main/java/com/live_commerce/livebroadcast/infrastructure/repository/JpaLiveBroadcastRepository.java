package com.live_commerce.livebroadcast.infrastructure.repository;

import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaLiveBroadcastRepository extends JpaRepository<LiveBroadcast, UUID>, LiveBroadcastRepository {

    Optional<LiveBroadcast> findByLiveBroadcastIdAndDeletedStatusFalse(UUID id);

    boolean existsByLiveBroadcastIdAndDeletedStatusFalse(UUID broadcastId);

    List<LiveBroadcast> findAllByDeletedStatusFalseAndBroadcastStatusIn(List<BroadcastStatus> statuses);

    @Query("SELECT lb.hostId FROM LiveBroadcast lb WHERE lb.liveBroadcastId = :id AND lb.deletedStatus = false")
    UUID findHostIdByBroadcastId(@Param("id") UUID broadcastId);

}
