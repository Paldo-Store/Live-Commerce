package com.live_commerce.payment.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.live_commerce.payment.domain.model.OutboxStatus;
import com.live_commerce.payment.domain.model.PaymentOutbox;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, UUID> {
	List<PaymentOutbox> findTop50ByStatusOrderByCreatedAt(OutboxStatus status);

	@Modifying
	@Query("UPDATE PaymentOutbox o SET o.status = :status, o.publishedAt = :publishedAt, o.retryCount = :retryCount WHERE o.id = :id")
	void updateRecord(@Param("id") UUID id,
		@Param("status") OutboxStatus status,
		@Param("publishedAt") LocalDateTime publishedAt,
		@Param("retryCount") int retryCount);
}
