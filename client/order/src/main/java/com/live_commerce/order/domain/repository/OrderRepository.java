package com.live_commerce.order.domain.repository;

import com.live_commerce.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByUserId(String userId, Pageable pageable);
}
