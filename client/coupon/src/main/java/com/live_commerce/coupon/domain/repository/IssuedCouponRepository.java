package com.live_commerce.coupon.domain.repository;

import com.live_commerce.coupon.domain.model.IssuedCoupon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, UUID> {

}
