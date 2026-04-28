package com.live_commerce.payment.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, PaymentQueryRepository {
	Optional<Payment> findByOrderId(UUID orderId);

	Optional<Payment> findByOrderIdAndStatus(UUID orderId, PaymentStatus paymentStatus);

	List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, LocalDateTime threshold);
}
