package com.live_commerce.livebroadcast.application.mapper;

import com.live_commerce.livebroadcast.application.dto.request.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.response.LiveBroadcastResponseDto;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.infrastructure.client.company.ExternalCompanyResponseDto;


public class LiveBroadcastMapper {

    public static LiveBroadcast createDtoToEntity(LiveBroadcastCreateRequestDto requestDto) {

        return LiveBroadcast.builder()
                .broadcastName(requestDto.broadcastName())
                .startTime(requestDto.startTime())
                .endTime(requestDto.endTime())
                .hostId(requestDto.hostId())
                .companyId(requestDto.companyId())
                .build();
    }

    public static LiveBroadcastResponseDto entityToDto(LiveBroadcast entity) {

        return LiveBroadcastResponseDto.builder()
                .liveBroadcastId(entity.getLiveBroadcastId())
                .broadcastName(entity.getBroadcastName())
                .broadcastStatus(entity.getBroadcastStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalViewerCount(entity.getTotalViewerCount())
                .hostId(entity.getHostId())
                .companyId(entity.getCompanyId())
                .build();
    }

}
