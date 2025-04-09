package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastUpdateRequestDto;
import com.live_commerce.livebroadcast.application.mapper.LiveBroadcastMapper;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveBroadcastService {

    private final LiveBroadcastRepository liveBroadcastRepository;

    @Transactional
    public LiveBroadcastResponseDto createBroadcast(LiveBroadcastCreateRequestDto requestDto) {
        LiveBroadcast liveBroadcast = LiveBroadcastMapper.createDtoToEntity(requestDto);
        liveBroadcastRepository.save(liveBroadcast);
        return LiveBroadcastMapper.entityToDto(liveBroadcast);
    }

    @Transactional(readOnly = true)
    public LiveBroadcastResponseDto getLiveBroadcast(UUID id) {
        LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(id)
                .filter(h -> !h.getDeletedStatus())
                .orElseThrow(() -> new IllegalArgumentException("No live broadcast exists with id: " + id));
        return LiveBroadcastMapper.entityToDto(liveBroadcast);
    }

    @Transactional
    public LiveBroadcastResponseDto updateLiveBroadcast(UUID id, LiveBroadcastUpdateRequestDto requestDto) {
        LiveBroadcast broadcast = liveBroadcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No live broadcast exists with id: " + id));

        broadcast.update(requestDto);
        return LiveBroadcastMapper.entityToDto(broadcast);
    }


}
