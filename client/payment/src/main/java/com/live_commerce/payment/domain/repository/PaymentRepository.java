package com.live_commerce.payment.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.live_commerce.payment.domain.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
	Optional<Payment> findByOrderId(UUID orderId);
}
