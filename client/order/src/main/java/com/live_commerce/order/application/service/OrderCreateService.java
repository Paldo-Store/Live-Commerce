package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.*;
import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckResponseDto;
import com.live_commerce.order.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

//주문 생성 service
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateService {
    private final BroadcastClient broadcastClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final CouponClient couponClient;

    //주문 생성 함수
    @Transactional
    public OrderCreateResponse orderCreator(OrderCreateRequest request, UUID userId) {

        // 0. 방송중인지 확인 - 방송중일때만 주문 가능
        ApiResponse<BroadcastStatusResponse> response = broadcastClient.getBroadcast(request.broadcastId());
        BroadcastStatusResponse statusResponse = response.getData();
        if (statusResponse == null || statusResponse.getBroadcastStatus() != BroadcastStatus.LIVE) {
            throw new OrderException("방송 중일 때만 주문이 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        log.info("방송 체크 완료");

        // 1. [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        // order -> product (productId, productQuantity)
        ApiResponse<OrderProductResponse> responseProduct = productClient.getProduct(request.productId()); //주문 요청 상품 id -> product
        OrderProductResponse productResponseByOrder = responseProduct.getData();

        // productId에 해당하는 상품이 아예 없는 경우
        if (productResponseByOrder == null) {
            throw new OrderException("해당 상품이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        //TODO product 로직으로 이동
        // 이미 품절된 상품일 경우,
        if(productResponseByOrder.getSoldOut()){
            throw new OrderException("해당 상품은 품절된 상품입니다", HttpStatus.BAD_REQUEST);
        }
        log.info("상품 조회 완료");

        // 재고가 주문보다 많나 확인 - 주문이 현재 가능한 상태인지 확인 로직
        // 2. 상품 재고 존재 여부 확인 로직
        ApiResponse<InventoryCheckResponseDto> responseInventory = inventoryClient.checkOrderableInventory(request.productId(), request.productQuantity());
        InventoryCheckResponseDto checkInventory = responseInventory.getData();
        if (!checkInventory.orderAvailable()) {
            throw new OrderException("재고가 없는 상태입니다.", HttpStatus.BAD_REQUEST);
        }

        log.info("재고 존재 여부 확인 완료");

        //TODO inventory 모듈에서
        // 1. productId 조회 후 상품 정보 가져오기
        // 2. 상품 재고 0개인지 확인
        // 3. 주문개수 < 재고개수 체크
        // 4. 토탈 주문 금액 계산

        // 3. 해당 상품의 남은 재고 수량 들고오기
        ApiResponse<InventoryCheckQuantityResponseDto> responseInventoryByQuantity = inventoryClient.checkInventoryQuantity(request.productId(), request.productQuantity());
        InventoryCheckQuantityResponseDto getInventoryQuantity = responseInventoryByQuantity.getData();

        int stock = getInventoryQuantity.availableQuantity(); // 실제 상품의 해당 재고 수량 (응답 product-> order)
        int orderQty = request.productQuantity(); // 사용자가 주문한 상품의 수량 (요청 order -> product)
        log.info("재고 수량 들고오기");

        // 4. 상품의 재고 수량이 0이거나, or 주문한 상품 개수 > 상품 재고 개수
        if ( (stock == 0) || (orderQty > stock)) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        log.info("재고 > 주문 수량");

        // 5. total 주문 금액 계산
        // 주문한 수량 * 주문한 상품 한 개의 가격
        Long productTotalPrice = (long) (orderQty * productResponseByOrder.getProductPrice());
        log.info("총 주문 금액 계산");

        // 6.  쿠폰에서 할인 적용할 금액 계산하도록 쿠폰아이디와 총 주문 금액(쿠폰 적용전) 넘겨주기
        // TODO 쿠폰에서 getDiscountAmount함수에 맞게 쿠폰 할인 금액 계산 API 생성
        Long finalPaidPrice = couponClient.getDiscountAmount(request.couponId(), productTotalPrice);
        log.info("할인 금액 적용한 최종 결제 예상 금액");

        // 7. 주문 생성 - 전체 물건 합과 userId는 따로 받아와야함
        Order order = request.toOrder(productTotalPrice, finalPaidPrice, userId);
        log.info("주문 생성 - 저장전");

        // 8. 생성 주문 저장 - 주문 생성 후, 상품 상태 PENDING
        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 저장 완료");
        return OrderCreateResponse.of(savedOrder);
    }
}
