package com.live_commerce.livebroadcast.application.service;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductListResponseDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductPageResponse;
import com.live_commerce.livebroadcast.application.mapper.BroadcastProductMapper;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.application.validation.ProductValidator;
import com.live_commerce.livebroadcast.domain.model.BroadcastProduct;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;

import com.live_commerce.livebroadcast.domain.repository.BroadcastProductQueryRepository;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.infrastructure.client.product.ExternalProductResponseDto;
import com.live_commerce.livebroadcast.infrastructure.client.product.ProductClient;
import com.live_commerce.livebroadcast.infrastructure.client.product.ProductSummaryDto;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BroadcastProductService {

    private final BroadcastProductRepository broadcastProductRepository;
    private final BroadcastProductQueryRepository broadcastProductQueryRepository;
    private final LiveBroadcastValidator liveBroadcastValidator;
    private final ProductValidator productValidator;

    private final ProductClient productClient;


    @Transactional
    public BroadcastProductResponseDto connectBroadcastProduct(UUID liveBroadcastId, BroadcastProductConnectDto requestDto) {

        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(liveBroadcastId);

        ExternalProductResponseDto productDto = productValidator.getValidProductOrThrow(requestDto.productId());

        liveBroadcastValidator.validateNotConnected(broadcast.getLiveBroadcastId(), productDto.productId());

        BroadcastProductConnectDto connectDto = BroadcastProductMapper.toConnectDto(broadcast, productDto);
        BroadcastProduct broadcastProduct = BroadcastProductMapper.connectDtoToEntity(connectDto);

        broadcastProductRepository.save(broadcastProduct);

        return BroadcastProductMapper.entityToDto(broadcastProduct);
    }

    @Transactional
    public void disconnectBroadcastProduct(UUID liveBroadcastId, UUID productId) {
        LiveBroadcast broadcast = liveBroadcastValidator.validateExists(liveBroadcastId);

        BroadcastProduct broadcastProduct = liveBroadcastValidator.validateConnectedProductExists(broadcast.getLiveBroadcastId(), productId);

        broadcastProduct.delete("temp");
    }

    @Transactional(readOnly = true)
    public BroadcastProductPageResponse getBroadcastProducts(UUID liveBroadcastId, Pageable pageable) {
        Page<UUID> productIds = broadcastProductQueryRepository.findProductIdsByBroadcastId(liveBroadcastId, pageable);

        //TODO validator 에다가 빼버려
        List<ProductSummaryDto> productSummaries = productClient.getProducts(productIds.getContent()).getData();

        Map<UUID, ProductSummaryDto> summaryMap = productSummaries.stream()
                .collect(Collectors.toMap(ProductSummaryDto::id, Function.identity()));

        List<BroadcastProductListResponseDto> content = productIds.getContent().stream()
                .map(id -> {
                    ProductSummaryDto summary = summaryMap.get(id);
                    return new BroadcastProductListResponseDto(id, summary.name(), summary.price());
                }).toList();

        Page<BroadcastProductListResponseDto> page = new PageImpl<>(content, pageable, productIds.getTotalElements());

        return BroadcastProductPageResponse.from(page);
    }


    @Transactional(readOnly = true)
    public boolean existsByBroadcastIdAndProductId(UUID liveBroadcastId, UUID productId) {
        liveBroadcastValidator.validateExists(liveBroadcastId);

        return broadcastProductRepository.existsByLiveBroadcastIdAndProductIdAndDeletedStatusFalse(liveBroadcastId, productId);
    }
}
