package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
import com.live_commerce.order.application.dto.response.OrderGetOneResponse;
import com.live_commerce.order.application.dto.response.OrderGetResponse;
import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.BroadcastClient;
import com.live_commerce.order.infrastructure.client.response.BroadcastStatusResponse;
import com.live_commerce.order.infrastructure.repository.OrderQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final BroadcastClient BroadcastClient;

    //주문 생성 service
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, String userId) {

        // 방송중인지 확인 - 방송중일때만 주문 가능
        BroadcastStatusResponse statusResponse = BroadcastClient.getBroadcastStatus(request.getBroadcastId());
        if(!"LIVE".equalsIgnoreCase(statusResponse.getBroadcastStatus())){
            throw new OrderException("방송 중일 때만 주문이 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        // productQuantity는 order에서 요청한 상품 수량.
        OrderProductResponse responseProduct = productClient.getProduct(
                request.getProductId(), request.getProductQuantity()); //요청한 id와 요청 수량을 product로 보낸다.
        if (responseProduct.isValid()) {
            throw new OrderException("해당 상품이 존재하지 않거나 재고가 충분하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        //TODO product 모듈에서
        // 1. productId 체크
        // 2. 상품 재고 0개인지 확인
        // 3. 주문개수 < 재고개수 체크

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
        Order company = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.NOT_FOUND));
        return OrderGetOneResponse.of(company);
    }
}
