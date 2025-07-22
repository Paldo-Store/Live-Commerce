package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastPageResponse;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastUpdateRequestDto;
import com.live_commerce.livebroadcast.application.manager.BroadcastAlarmManager;
import com.live_commerce.livebroadcast.application.mapper.LiveBroadcastMapper;
import com.live_commerce.livebroadcast.application.validation.CompanyValidator;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.application.validation.PermissionValidator;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import com.live_commerce.livebroadcast.domain.repository.query.LiveBroadcastQueryRepository;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveBroadcastService {

    private final LiveBroadcastRepository liveBroadcastRepository;
    private final LiveBroadcastValidator liveBroadcastValidator;
    private final CompanyValidator companyValidator;
    private final LiveBroadcastQueryRepository liveBroadcastQueryRepository;
    private final PermissionValidator permissionValidator;
    private final BroadcastAlarmManager broadcastAlarmManager;

    @Transactional
    public LiveBroadcastResponseDto createBroadcast(LiveBroadcastCreateRequestDto requestDto) {
        liveBroadcastValidator.validateCreatableRequest(requestDto);
        companyValidator.validateExistsAndActiveOrThrow(requestDto.companyId());
        LiveBroadcast liveBroadcast = LiveBroadcastMapper.createDtoToEntity(requestDto);
        liveBroadcastRepository.save(liveBroadcast);
        return LiveBroadcastMapper.entityToDto(liveBroadcast);
    }

    @Transactional(readOnly = true)
    public LiveBroadcastResponseDto getLiveBroadcast(UUID id) {
        LiveBroadcast liveBroadcast = liveBroadcastValidator.validateExists(id);
        return LiveBroadcastMapper.entityToDto(liveBroadcast);
    }

    @Transactional
    public LiveBroadcastResponseDto updateLiveBroadcast(UUID id, LiveBroadcastUpdateRequestDto requestDto, RequestUserDetails userDetails) {
        LiveBroadcast liveBroadcast = liveBroadcastValidator.validateExists(id);
        permissionValidator.validateOwnerOrMaster(userDetails, id);
        liveBroadcastValidator.validateUpdateRequest(requestDto, liveBroadcast);
        liveBroadcast.update(requestDto);

        broadcastAlarmManager.delete(id);
        broadcastAlarmManager.register(id, liveBroadcast.getStartTime());

        return LiveBroadcastMapper.entityToDto(liveBroadcast);
    }

    @Transactional
    public void deleteBroadcast(UUID id, RequestUserDetails userDetails) {
        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(id);
        permissionValidator.validateOwnerOrMaster(userDetails, id);
        broadcast.delete(UUID.randomUUID());
        broadcastAlarmManager.delete(userDetails.getUserId());
    }

    @Transactional(readOnly = true)
    public LiveBroadcastPageResponse searchLiveBroadcast(String keyword, Pageable pageable) {
        int requestedSize = pageable.getPageSize();
        int validSize = switch (requestedSize) {
            case 30, 50 -> requestedSize;
            default -> 10;
        };

        Pageable validatedPageable = PageRequest.of(
                pageable.getPageNumber(),
                validSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<LiveBroadcastResponseDto> page = liveBroadcastQueryRepository
                .searchByBroadcastName(keyword, validatedPageable);

        return LiveBroadcastPageResponse.from(page);
    }

}
