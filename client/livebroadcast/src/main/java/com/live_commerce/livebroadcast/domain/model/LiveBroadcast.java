package com.live_commerce.livebroadcast.domain.model;


import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_live_broadcast", schema = "livebroadcast")
public class LiveBroadcast extends BaseEntity {

    @Id
    @UuidGenerator
    private UUID liveBroadcastId;

    @Column(nullable = false)
    private String broadcastName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BroadcastStatus broadcastStatus;

    private UUID hostId;

    private UUID companyId;

    private Integer totalViewerCount;

    @Builder
    private LiveBroadcast(String broadcastName, LocalDateTime startTime, LocalDateTime endTime, UUID hostId, UUID companyId) {
        this.broadcastName = broadcastName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.broadcastStatus = BroadcastStatus.SCHEDULED;
        this.hostId = hostId;
        this.companyId = companyId;
        this.totalViewerCount = 0;
    }

    public static LiveBroadcast create(String broadcastName, LocalDateTime startTime, LocalDateTime endTime, UUID hostId, UUID companyId) {
        return new LiveBroadcast(broadcastName, startTime, endTime, hostId, companyId);
    }

    public void update(LiveBroadcastUpdateRequestDto dto) {
        if (dto.broadcastName() != null) this.broadcastName = dto.broadcastName();
        if (dto.startTime() != null) this.startTime = dto.startTime();
        if (dto.endTime() != null) this.endTime = dto.endTime();
        if (dto.broadcastStatus() != null) this.broadcastStatus = dto.broadcastStatus();
    }

}
