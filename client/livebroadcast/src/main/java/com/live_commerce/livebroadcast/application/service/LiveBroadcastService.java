package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateResponseDto;
import com.live_commerce.livebroadcast.application.mapper.LiveBroadcastMapper;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LiveBroadcastService {

    private final LiveBroadcastRepository liveBroadcastRepository;

    public LiveBroadcastCreateResponseDto createBroadcast(LiveBroadcastCreateRequestDto requestDto) {
        LiveBroadcast liveBroadcast = LiveBroadcastMapper.createDtoToEntity(requestDto);
        liveBroadcastRepository.save(liveBroadcast);
        return LiveBroadcastMapper.entityToCreateDto(liveBroadcast);
    }

}
