package com.live_commerce.product.product.infrastructure.kafka.consumer;


import com.live_commerce.product.inventory.infrastructure.kafka.event.InventorySoldOutEvent;
import com.live_commerce.product.product.application.validation.ProductValidator;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.model.ProductStatus;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductSoldOutListener {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;

    @Transactional
    @KafkaListener(topics = "inventory-sold-out", groupId = "product-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeSoldOut(InventorySoldOutEvent event) {
        log.info("inventory-sold-out 이벤트 수신: {}", event);

        Product product = productValidator.validateAndFindProduct(event.productId());

        product.changeStatus(ProductStatus.SOLD_OUT);

        productRepository.save(product);
    }
}
