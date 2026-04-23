package com.live_commerce.product.inventory;


import com.live_commerce.product.inventory.application.service.InventoryService;
import com.live_commerce.product.inventory.domain.exception.InventoryException;
import com.live_commerce.product.inventory.domain.model.Inventory;
import com.live_commerce.product.inventory.domain.model.InventoryStatus;
import com.live_commerce.product.inventory.domain.repository.InventoryRepository;
import com.live_commerce.product.inventory.infrastructure.repository.JpaInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ActiveProfiles("test")
public class InventoryServiceTest {

    private static final UUID PRODUCT_ID = UUID.fromString("a8e5b7f9-bc53-4b3d-a2d2-d2513fa44b57");
    private static final int INITIAL_QUANTITY = 100;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private JpaInventoryRepository jpaInventoryRepository;

    @BeforeEach
    @Rollback(false)
    void setUp() {
        if (inventoryRepository.findByProductIdAndDeletedStatusFalse(PRODUCT_ID).isEmpty()) {
            Inventory inventory = Inventory.create(
                    PRODUCT_ID,
                    INITIAL_QUANTITY,
                    0,
                    INITIAL_QUANTITY,
                    InventoryStatus.AVAILABLE
            );
            inventoryRepository.save(inventory);
        }
    }

    @Test
    void 동시_재고감소_테스트() throws InterruptedException {

        System.out.println("=== BEFORE TEST ===");
        jpaInventoryRepository.findAll().forEach(i ->
                System.out.println("재고 있음: " + i.getProductId() + ", 삭제 상태: " + i.isDeletedStatus()));

        int threadCount = 10;
        int decreaseAmountPerThread = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    inventoryService.decreaseInventory(PRODUCT_ID, decreaseAmountPerThread);
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료까지 대기

        // 이후 재고 수량 검증
        Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(PRODUCT_ID)
                .orElseThrow(() -> new RuntimeException("재고가 존재하지 않습니다."));

        assertEquals(
                INITIAL_QUANTITY - (threadCount * decreaseAmountPerThread),
                inventory.getAvailableQuantity()
        );
    }

    @Test
    void 재고초과요청시_예외발생_테스트() throws InterruptedException {
        System.out.println("=== BEFORE OVER-REQUEST TEST ===");
        jpaInventoryRepository.findAll().forEach(i ->
                System.out.println("재고 있음: " + i.getProductId() + ", 삭제 상태: " + i.isDeletedStatus()));

        int threadCount = 150; // 초기 재고 100보다 더 많은 요청
        int decreaseAmountPerThread = 1;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    inventoryService.decreaseInventory(PRODUCT_ID, decreaseAmountPerThread);
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 실패 카운트 증가
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Inventory inventory = inventoryRepository.findByProductIdAndDeletedStatusFalse(PRODUCT_ID)
                .orElseThrow(() -> new RuntimeException("재고가 존재하지 않습니다."));

        System.out.println("최종 재고: " + inventory.getAvailableQuantity());
        System.out.println("실패 요청 수: " + failCount.get());

        assertEquals(0, inventory.getAvailableQuantity()); // 재고는 0이어야 하고
        assertEquals(threadCount - INITIAL_QUANTITY, failCount.get()); // 실패 수 == 초과 요청 수
    }


    @Test
    void 락획득실패_테스트() throws InterruptedException {
        UUID productId = PRODUCT_ID;

        CountDownLatch latch = new CountDownLatch(2);

        // 1. 스레드1이 락을 잡고 오래 점유
        new Thread(() -> {
            try {
                inventoryService.lockForTesting(productId, 5); // sleep 5초
            } catch (Exception e) {
                System.out.println("Thread1 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        }).start();

        Thread.sleep(500); // 락 선점 보장

        // 2. 스레드2가 락을 못잡고 실패
        new Thread(() -> {
            try {
                inventoryService.lockForTesting(productId, 0); // 바로 실패해야함
            } catch (InventoryException e) {
                System.out.println("Thread2 예외 발생: " + e.getMessage());
                assertEquals("현재 처리 중입니다. 잠시 후 다시 시도해주세요.", e.getMessage());
            } finally {
                latch.countDown();
            }
        }).start();

        latch.await();
    }

}
