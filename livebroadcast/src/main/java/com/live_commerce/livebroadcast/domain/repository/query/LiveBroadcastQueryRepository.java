package com.live_commerce.livebroadcast.domain.repository.query;

import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LiveBroadcastQueryRepository {

    Page<LiveBroadcastResponseDto> searchByBroadcastName(String keyword, Pageable pageable);
}
