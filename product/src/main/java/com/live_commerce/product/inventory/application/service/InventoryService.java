package com.live_commerce.product.inventory.application.service;


import com.live_commerce.product.inventory.application.dto.request.InventoryCreateRequestDto;
import com.live_commerce.product.inventory.application.dto.request.InventoryDecreaseRequestDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryCheckOrderableResponseDto;
import com.live_commerce.product.inventory.application.dto.response.InventoryResponseDto;
import com.live_commerce.product.inventory.application.mapper.InventoryMapper;
import com.live_commerce.product.inventory.application.validation.InventoryValidator;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.model.InventoryStatus;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.product.infrastructure.kafka.event.InventorySoldOutEvent;
import com.live_commerce.product.product.domain.repository.ProductRepository;
import com.live_commerce.product.inventory.infrastructure.redisson.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryValidator inventoryValidator;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private static final String STOCK_KEY_PREFIX = "inventory:stock:";

    @Transactional
    public InventoryResponseDto createInventory(InventoryCreateRequestDto requestDto) {
        if (!productRepository.existsByProductIdAndDeletedStatusFalse(requestDto.productId())) {
            throw InventoryException.forProductNotFound();
        }

        Inventory inventory = InventoryMapper.createDtoToEntity(requestDto);
        inventoryRepository.save(inventory);

        // Redis 재고 초기화
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + inventory.getProductId(), String.valueOf(inventory.getAvailableQuantity()));

        return InventoryMapper.entityToDto(inventory);
    }

    @Transactional
    public void decreaseInventoryWithLua(UUID productId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;

        // Lua Script: 재고 확인 및 차감
        // 리턴 값: 차감 후 재고 (>=0), -1: 키 없음, -2: 재고 부족
        String luaScript =
                "local stock = redis.call('get', KEYS[1]) " +
                "if not stock then return -1 end " +
                "if tonumber(stock) < tonumber(ARGV[1]) then return -2 end " +
                "return redis.call('decrby', KEYS[1], ARGV[1])";

        Long result = redissonClient.getScript()
                .eval(RScript.Mode.READ_WRITE,
                        luaScript,
                        RScript.ReturnType.INTEGER,
                        Collections.singletonList(stockKey),
                        quantity);

        if (result == -1) {
            // Redis에 재고가 없으면 DB에서 로드 후 재시도 (또는 예외 처리)
            Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(productId)
                    .orElseThrow(InventoryException::forInventoryNotFound);
            redisTemplate.opsForValue().set(stockKey, String.valueOf(inventory.getAvailableQuantity()));
            
            // 로드 후 다시 시도
            result = redissonClient.getScript()
                    .eval(RScript.Mode.READ_WRITE,
                            luaScript,
                            RScript.ReturnType.INTEGER,
                            Collections.singletonList(stockKey),
                            quantity);
        }

        if (result == -2) {
            throw InventoryException.forInventoryOutOfStock();
        }

        if (result == -1) { // 로드 후에도 없으면
             throw InventoryException.forInventoryNotFound();
        }

        // DB 재고 업데이트
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getInventory(UUID id) {
        Inventory inventory = inventoryRepository.findByInventoryIdAndDeletedStatusFalse(id).orElse(null);

        if (inventory == null) {
            throw InventoryException.forInventoryNotFound();
        }

        return InventoryMapper.entityToDto(inventory);
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void decreaseInventory(UUID productId, int quantity) {
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void increaseInventory(UUID productId, int quantity) {
        int updated = inventoryRepository.increaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryNotFound();
        }
    }

    @DistributedLock(key = "#productId")
    @Transactional
    public void increaseInventoryV2(UUID productId, int quantity) {
        int updated = inventoryRepository.increaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryNotFound();
        }
    }

    @Transactional
    @DistributedLock(key = "#productId")
    public void decreaseInventoryV2(UUID productId, int quantity) {
        int updated = inventoryRepository.decreaseInventoryAtomically(productId, quantity);
        if (updated == 0) {
            throw InventoryException.forInventoryOutOfStock();
        }

        String soldCountKey = "product:sold_count:" + productId;
        redisTemplate.opsForValue().increment(soldCountKey, quantity);

        Inventory inventory = inventoryValidator.validateAndGetActiveInventory(productId);

        if (inventory.getAvailableQuantity() == 0) {
            inventory.changeStatus(InventoryStatus.OUT_OF_STOCK);

            InventorySoldOutEvent soldOutEvent = new InventorySoldOutEvent(productId);
            kafkaTemplate.send("inventory-sold-out", soldOutEvent);
            log.info("inventory-sold-out 이벤트 발행 완료: {}", soldOutEvent);
        }
    }

    public boolean isSoldOut(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(productId)
                .orElse(null);

        if (inventory == null) return true;

        return inventory.getQuantity() <= 0;
    }


    public InventoryCheckQuantityResponseDto checkInventoryQuantity(UUID productId) {
        Inventory inventory = inventoryValidator.validateAndGetActiveInventory(productId);
        return InventoryMapper.toCheckQuantityDto(inventory);
    }

    public InventoryCheckOrderableResponseDto checkOrderableInventory(UUID productId, int orderQuantity) {
        boolean orderable = inventoryValidator.checkOrderable(productId, orderQuantity);
        return InventoryMapper.toCheckResponseDto(orderable);
    }

    @DistributedLock(key = "#productId", waitTime = 1, leaseTime = 2) // 강제 제한
    public void lockForTesting(UUID productId, int holdSeconds) {
        try {
            Thread.sleep(holdSeconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
