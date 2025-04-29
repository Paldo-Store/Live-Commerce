package com.live_commerce.order.kafkaOrder.service;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.request.OrderStatusUpdateRequest;
import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.*;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.application.service.OrderModificationService;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.feign.CouponClient;
import com.live_commerce.order.infrastructure.client.feign.ProductClient;
import com.live_commerce.order.infrastructure.client.request.InventoryDecreaseRequestDto;
import com.live_commerce.order.infrastructure.repository.OrderQueryRepository;
import com.live_commerce.order.kafkaOrder.coupon.CouponUsedMessage;
import com.live_commerce.order.kafkaOrder.coupon.CouponUsedProducer;
import com.live_commerce.order.kafkaOrder.payment.PaymentCompletedEvent;
import com.live_commerce.order.kafkaOrder.product.InventoryEventProducer;
import com.live_commerce.order.kafkaOrder.product.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.live_commerce.order.domain.model.OrderStatus.PAID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceKafka {
    //DB 조회
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    //service 호출
    private final OrderCreateServiceKafka orderCreateServiceKafka;
    @Lazy
    private final PaymentStatusTransitionServiceKafka paymentStatusTransitionServiceKafka;  //@Lazy 적용
    private final OrderModificationService orderModificationService;

    //feign 요청
    private final ProductClient productClient;
    private final CouponClient couponClient;

    //kafka
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final InventoryEventProducer inventoryEventProducer;
    private final CouponUsedProducer couponUsedProducer;

    //주문 생성 service
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, UUID userId) {
        return orderCreateServiceKafka.orderCreator(request, userId);
    }

    //주문 전체 조회 service
    @Transactional(readOnly = true)
    public OrderGetResponse getOrders(final int page, final int size, final String sort, UUID userId, String role){
        Pageable pageable = getPageable(page, size, sort);

        //권한 검증 - CUSTOMER 본인 주문만 조회 가능, 나머지 권한 다 조회 가능
        if ("ROLE_CUSTOMER".equals(role)) {
            // 고객: 본인 주문만 조회
            return OrderGetResponse.of(orderRepository.findAllByUserId(userId, pageable));
        } else {
            // 나머지 : 전체 주문 조회
            return OrderGetResponse.of(orderQueryRepository.findAll(pageable));
        }
    }

    //주문 단건 조회 service
    @Transactional(readOnly = true)
    public OrderGetOneResponse getOrder(final UUID id, UUID userId, String role) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));

        // 권한 검증 - CUSTOMER는 본인 주문만 조회 가능
        validateCustomerOrderAccess(role, order.getUserId(), userId);

        return OrderGetOneResponse.of(order);
    }

    //주문 수정 service - 주문 상태 변경을 일어나지 않음.
    //주문 개수, 상품 id, 요청 사항만 수정 가능
    @Transactional
    public OrderUpdateResponse updateOrder(UUID orderId, OrderUpdateRequest request, UUID userId, String role) {
        return orderModificationService.updateCreator(orderId, request, userId, role);
    }

    //주문 상태 변경 SERVICE
    //고객 제외 나머지가 주문 상태 변경
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request, UUID userId, String role) {
        return paymentStatusTransitionServiceKafka.updateCreator(orderId, request, userId, role);
    }

    //주문 삭제 SERVICE
    @Transactional
    public OrderDeleteResponse deleteOrder(UUID orderId, UUID userId, String role) {
        // orderId에 해당하는 Order 검색
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));

        // 권한 검증 - 고객일 경우 본인 결제 내역만 수정 가능하게
        validateCustomerOrderAccess(role, order.getUserId(), userId);

        // 삭제 진행
        order.delete(userId.toString());
        return OrderDeleteResponse.of(order.getId());
    }

    //페이징 함수
    private Pageable getPageable(final int page, final int size, final String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size); // 기본 정렬 없음
        }
        String[] sortParams = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String param : sortParams) {
            String[] fieldAndDirection = param.trim().split("[- ]"); // '-' 또는 ' '으로 구분
            if (fieldAndDirection.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid sort parameter format. Expected 'field direction' (e.g., 'name asc').");
            }
            String field = fieldAndDirection[0].trim();
            String direction = fieldAndDirection[1].trim().toUpperCase();
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw new IllegalArgumentException("Invalid sort direction. Use 'asc' or 'desc'.");
            }
            Sort.Direction dir = Sort.Direction.fromString(direction);
            orders.add(new Sort.Order(dir, field));
        }
        Sort sortObj = Sort.by(orders);
        return PageRequest.of(page, size, sortObj);
    }

    //고객의 경우 본인의 주문만 수정가능
    public void validateCustomerOrderAccess(String role, UUID orderUserId, UUID currentUserId) {
        if ("ROLE_CUSTOMER".equals(role) && !orderUserId.equals(currentUserId)) {
            throw new OrderException("고객은 자신의 주문만 조회, 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
    }

    //TODO KAFKA
    //결제 처리 응답값 가져오기
    @Transactional
    public String updatePaymentSuccessKafka(PaymentCompletedEvent event){
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));
        log.info("주문 들고오기 성공");
        if(event.message() == null){
            throw new OrderException("결제 상태가 COMPLETED가 아닌 상태입니다. 다시 결제해주세요 ", HttpStatus.FORBIDDEN);
        }

        //상태 변경 PAID로 변경
        order.changeStatus(PAID);
        log.info("READY 에서 PAID로 변경 성공!");

        //TODO KAFKA 처리
        //재고 감소 진행
        //productClient.decreaseInventory(new InventoryDecreaseRequestDto(order.getProductId(), order.getProductQuantity()));
        OrderCreatedEvent eventInventory = new OrderCreatedEvent(order.getId(), order.getProductId(), order.getProductQuantity());
        inventoryEventProducer.sendOrderCreatedEvent(eventInventory);
        log.info("재고 감소 성공!!");

        //TODO KAFKA
        // 7. 쿠폰 사용 처리
//        if (order.getCouponId() != null) {
//            couponClient.useCoupon(order.getCouponId());
//        }
        UUID userId = UUID.randomUUID();
        if(order.getCouponId() != null){
            CouponUsedMessage eventCoupon = new CouponUsedMessage(order.getCouponId(), userId);
            couponUsedProducer.sendCouponUsedEvent(eventCoupon);
        }
        return "결제 성공 처리 완료";
    }

    //kafka
    public void sendMessage(String topic , String key, String message) {
        for (int i = 0; i < 10; i++) {
            kafkaTemplate.send(topic, key, message + " " + i);
        }
    }
}
