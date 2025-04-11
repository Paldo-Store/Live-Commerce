package com.live_commerce.livebroadcast.application.mapper;

import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;

public class BroadcastProductMapper {

    public static BroadcastProductResponseDto entityToDto(BroadcastProduct entity) {
        return new BroadcastProductResponseDto(
                entity.getId(),
                entity.getBroadcastId(),
                entity.getProductId()
        );
    }

    public static BroadcastProduct dtoToEntity(BroadcastProductResponseDto dto) {
        return BroadcastProduct.create(dto.broadcastId(), dto.productId());
    }
}
