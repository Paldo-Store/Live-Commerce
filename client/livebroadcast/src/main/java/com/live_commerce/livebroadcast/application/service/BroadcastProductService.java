package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.mapper.BroadcastProductMapper;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.infrastructure.client.ExternalProductResponseDto;
import com.live_commerce.livebroadcast.infrastructure.client.ProductClient;
import com.live_commerce.livebroadcast.presentation.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BroadcastProductService {

    private final BroadcastProductRepository broadcastProductRepository;
    private final LiveBroadcastValidator liveBroadcastValidator;
    private final ProductClient productClient;

    @Transactional
    public BroadcastProductResponseDto connectBroadcastProduct(UUID broadcastId, BroadcastProductConnectDto requestDto) {

        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(broadcastId);

        ApiResponse<ExternalProductResponseDto> productResponse = productClient.getProduct(requestDto.productId());
        // data 필드로 꺼내기
        if (productResponse == null || productResponse.getData() == null) {
            throw LiveBroadcastException.forExternalProductNotFound();
        }
        ExternalProductResponseDto productResponseDto = productResponse.getData();

        liveBroadcastValidator.validateNotConnected(broadcast.getId(), productResponseDto.productId());

        BroadcastProduct broadcastProduct = BroadcastProduct.create(broadcast.getId(), productResponseDto.productId());
        broadcastProductRepository.save(broadcastProduct);

        return BroadcastProductMapper.entityToDto(broadcastProduct);
    }

}
