package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.*;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.BroadcastClient;
import com.live_commerce.order.infrastructure.client.ProductClient;
import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import com.live_commerce.order.infrastructure.repository.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final BroadcastClient BroadcastClient;
    private final ProductClient productClient;

    //주문 생성 service
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, String userId) {

        // 방송중인지 확인 - 방송중일때만 주문 가능
        BroadcastStatusResponse statusResponse = BroadcastClient.getBroadcastStatus(request.broadcastId());
        if(!"LIVE".equalsIgnoreCase(statusResponse.getBroadcastStatus())){
            throw new OrderException("방송 중일 때만 주문이 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        OrderProductResponse responseProduct = productClient.getProduct(
                request.productId(),
                request.productQuantity()); //주문 요청 상품 id, 상품 주문 개수 -> product

        // 이미 삭제(단종)된 상품일 경우
        if(responseProduct.getDeletedStatus()){
            throw new OrderException("해당 상품은 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        int orderQty = request.productQuantity(); // 사용자가 주문한 상품의 수량 (요청 order -> product)
        int stock = responseProduct.getProductQuantity(); // 실제 상품의 해당 재고 수량 (응답 product-> order)
        //상품의 재고 수량이 0이거나, or 주문한 상품 개수 > 상품 재고 개수
        if (stock == 0 || orderQty > stock) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        //TODO product 모듈에서
        // 1. productId 체크
        // 2. 상품 재고 0개인지 확인
        // 3. 주문개수 < 재고개수 체크
        // 4. 토탈 주문 금액 계산

        //TODO
        // 1. 결제시 재고 차감이 이루어지도록
        // 2. 결제 후 쿠폰 적용

        // 주문 생성 - 전체 물건 합과 userId는 따로 받아와야함
        Long productTotalPrice = responseProduct.getProductTotalPrice();
        Order order = request.toOrder(productTotalPrice, userId);

        // 생성 주문 저장
        Order savedOrder = orderRepository.save(order);
        return OrderCreateResponse.of(savedOrder);
    }

    //주문 전체 조회 service
    @Transactional(readOnly = true)
    public OrderGetResponse getOrders(final int page, final int size, final String sort){
        //TODO 권한 검증 - CUSTOMER 본인 주문만 조회 가능, 나머지 권한 다 조회 가능

        Pageable pageable = getPageable(page, size, sort);
        return OrderGetResponse.of(orderQueryRepository.findAll(pageable));
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
    
    //주문 단건 조회 service
    @Transactional(readOnly = true)
    public OrderGetOneResponse getOrder(final UUID id) {
        //TODO CUSTOMER 본인 단건 조회만 가능, 나머지 권한 다 조회 가능

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));
        return OrderGetOneResponse.of(order);
    }

    //주문 수정 service
    @Transactional
    public OrderUpdateResponse updateOrder(UUID orderId, OrderUpdateRequest request, String userId, String role) {

        // orderId에 해당하는 주문 가져오기
        Order order  = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderException(OrderExceptionCode.NOT_FOUND));

        //TODO 권한 검증 - 본인 결제 내역만 수정 가능하게
        // 고객인 경우에만 자신의 주문만 수정 가능
        if (role =="CUSTOMER" && !order.getUserId().equals(userId)) {
            throw new OrderException("고객은 자신의 주문만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 현재 주문의 상태가 결제 완료거나 주문 접수인 경우만 수정 가능하도록 설정하기
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.PAID && status != OrderStatus.PENDING) {
            throw new OrderException("주문 내역을 수정할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        OrderProductResponse responseProduct = productClient.getProduct(
                request.productId(),
                request.productQuantity()); //주문 요청 상품 id, 상품 주문 개수 -> product

        // 이미 삭제(단종)된 상품일 경우
        if(responseProduct.getDeletedStatus()){
            throw new OrderException("해당 상품은 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        int orderQty = request.productQuantity(); // 사용자가 주문한 상품의 수량 (요청 order -> product)
        int stock = responseProduct.getProductQuantity(); // 실제 상품의 해당 재고 수량 (응답 product-> order)

        //상품의 재고 수량이 0이거나, or 주문한 상품 개수 > 상품 재고 개수
        if (stock == 0 || orderQty > stock) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        //총 합계 금액 수정
        Long productTotalPrice = responseProduct.getProductTotalPrice();

        //주문 상태 수정
        Order updateOrder = request.toOrder(productTotalPrice);
        order.updateOrder(updateOrder);

        return OrderUpdateResponse.fromOrder(order);
    }
}
