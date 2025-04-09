package com.live_commerce.order.presentation.controller;

import com.live_commerce.order.application.dto.request.OrderCreateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Slf4j
@RequestMapping("/api/v1/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //주문 생성 API
    @PostMapping("/")
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(
            @Valid @RequestBody final OrderCreateRequest request){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        OrderCreateResponse response = orderService.createOrder(request, userId);
        return ResponseUtil.success(response);
    }
}
