package com.live_commerce.order.application.dto.response;

import com.live_commerce.order.domain.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

public record OrderGetResponse(List<OrderCreateResponse> companies, int totalPages,
                                 long totalElements) {

    public static OrderGetResponse of(Page<Order> orderPages) {
        return new OrderGetResponse(orderPages.map(OrderCreateResponse::of).toList(),
                orderPages.getTotalPages(), orderPages.getTotalElements());
    }
}



