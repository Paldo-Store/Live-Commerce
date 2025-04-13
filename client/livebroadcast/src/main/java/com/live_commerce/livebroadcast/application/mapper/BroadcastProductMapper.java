package com.live_commerce.livebroadcast.application.mapper;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.infrastructure.client.product.ExternalProductResponseDto;

public class BroadcastProductMapper {

    public static BroadcastProductResponseDto entityToDto(BroadcastProduct entity) {
        return new BroadcastProductResponseDto(
                entity.getBroadcastProductId(),
                entity.getLiveBroadcastId(),
                entity.getProductId()
        );
    }

    public static BroadcastProduct connectDtoToEntity(BroadcastProductConnectDto dto) {
        return BroadcastProduct.create(dto.liveBroadcastId(), dto.productId());
    }

    public static BroadcastProductConnectDto toConnectDto(LiveBroadcast broadcast, ExternalProductResponseDto productDto) {
        return new BroadcastProductConnectDto(
                broadcast.getLiveBroadcastId(),
                productDto.productId()
        );
    }
}
