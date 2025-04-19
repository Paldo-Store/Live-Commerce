package com.live_commerce.order.presentation.controller;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.request.OrderStatusUpdateRequest;
import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.*;
import com.live_commerce.order.application.service.OrderService;
import com.live_commerce.order.infrastructure.client.PaymentFailRequest;
import com.live_commerce.order.infrastructure.client.PaymentSuccessRequest;
import com.live_commerce.order.infrastructure.client.PaymentSuccessResponseOrder;
import com.live_commerce.order.infrastructure.common.ResponseUtil;
import com.live_commerce.order.infrastructure.security.RequestUserDetails;
import com.live_commerce.order.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RequestMapping("/api/v1/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    //주문 생성 API
    //누구나 주문 가능
    @PostMapping("")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody final OrderCreateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails){

        //주문한 사람 id 받아오기
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderCreateResponse response = orderService.createOrder(request, userId);
        return ResponseUtil.success(response);
    }

    //주문 전체 조회 API
    @GetMapping("")
    public ResponseEntity<ApiResponse<OrderGetResponse>> getOrders(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort,
            @AuthenticationPrincipal RequestUserDetails userDetails){
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderGetResponse response = orderService.getOrders(page, size, sort, userId, role);
        return ResponseUtil.success(response);
    }

    //주문 단건 조회 API
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderGetOneResponse>> getOrder(
            @PathVariable final UUID orderId,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderGetOneResponse response = orderService.getOrder(orderId, userId, role);
        return ResponseUtil.success(response);
    }

    //주문 수정 API
    //주문 수정 - 상품 개수 ~
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderUpdateResponse>> updateOrder(
            @PathVariable final UUID orderId,
            @Valid @RequestBody final OrderUpdateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails){
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderUpdateResponse response = orderService.updateOrder(orderId, request, userId, role);
        return ResponseUtil.success(response);
    }

    //주문 상태 변경 API
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody @Valid OrderStatusUpdateRequest request,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderId, request, userId, role);
        return ResponseUtil.success(response);
    }

    //주문 내역 삭제 API -> softDeleted만 이루어진다. 주문 상태 변경없음.(결제 취소 발생하면 안된다)
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDeleteResponse>>  deleteOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal RequestUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        OrderDeleteResponse response = orderService.deleteOrder(orderId, userId, role);
        return ResponseUtil.success(response);
    }

    //payment -> order 로 받는 controller 생성
    // 결제 성공 응답 받는 Api
    @PostMapping("/{orderId}/payment-success")
    public ResponseEntity<ApiResponse<PaymentSuccessResponseOrder>> notifyPaymentSuccess(
            @PathVariable UUID orderId,
            @RequestBody PaymentSuccessRequest request){
        PaymentSuccessResponseOrder response=  orderService.updatePaymentSuccess(orderId, request);
        return ResponseUtil.success(response);
    }
}
