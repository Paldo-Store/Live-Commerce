package com.live_commerce.coupon.domain.repository;

import com.live_commerce.coupon.domain.model.CouponPolicy;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, UUID> {

  Boolean existsByName(String name);

  Optional<CouponPolicy> findByCodeAndDeletedStatusFalse(UUID id);
}