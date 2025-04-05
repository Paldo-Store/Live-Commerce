package com.live_commerce.livebroadcast.application.mapper;

import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateResponseDto;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;


public class LiveBroadcastMapper {

    public static LiveBroadcast createDtoToEntity(LiveBroadcastCreateRequestDto requestDto) {

        return LiveBroadcast.builder()
                .broadcastName(requestDto.getBroadcastName())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .hostId(requestDto.getHostId())
                .companyId(requestDto.getCompanyId())
                .build();
    }

    public static LiveBroadcastCreateResponseDto entityToCreateDto(LiveBroadcast entity) {

        return LiveBroadcastCreateResponseDto.builder()
                .id(entity.getId())
                .broadcastName(entity.getBroadcastName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalViewerCount(entity.getTotalViewerCount())
                .hostId(entity.getHostId())
                .companyId(entity.getCompanyId())
                .build();
    }

}
