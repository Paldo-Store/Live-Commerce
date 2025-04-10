package com.live_commerce.order.presentation.controller;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
import com.live_commerce.order.application.dto.response.OrderGetOneResponse;
import com.live_commerce.order.application.dto.response.OrderGetResponse;
import com.live_commerce.order.application.dto.response.OrderUpdateResponse;
import com.live_commerce.order.application.service.OrderService;
import com.live_commerce.order.infrastructure.common.ResponseUtil;
import com.live_commerce.order.presentation.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PostMapping("/")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody final OrderCreateRequest request){

        //주문한 사람 id 받아오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        OrderCreateResponse response = orderService.createOrder(request, userId);
        return ResponseUtil.success(response);
    }

    //주문 전체 조회 API
    @GetMapping("/")
    public ResponseEntity<ApiResponse<OrderGetResponse>> getOrders(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam(required = false) final String sort){
        OrderGetResponse response = orderService.getOrders(page, size, sort);
        return ResponseUtil.success(response);
    }

    //주문 단건 조회 API
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderGetOneResponse>> getOrder(
            @PathVariable final UUID orderId) {
        OrderGetOneResponse response = orderService.getOrder(orderId);
        return ResponseUtil.success(response);
    }

    //주문 수정 API
    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderUpdateResponse>> updateOrder(
            @PathVariable final UUID orderId,
            @Valid @RequestBody final OrderUpdateRequest request){

        //주문한 사람 id 받아오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("권한이 없습니다."));

        OrderUpdateResponse response = orderService.updateOrder(orderId, request, userId, role);
        return ResponseUtil.success(response);
    }

    //주문 취소 API

    //주문 내역 삭제 API

}
