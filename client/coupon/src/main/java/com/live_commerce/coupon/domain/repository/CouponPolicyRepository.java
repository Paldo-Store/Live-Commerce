package com.live_commerce.coupon.domain.repository;

import com.live_commerce.coupon.domain.model.CouponPolicy;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, String> {

  Optional<CouponPolicy> findByCodeAndDeletedStatusFalse(String code);

  List<CouponPolicy> findByDeletedStatusFalse();
}