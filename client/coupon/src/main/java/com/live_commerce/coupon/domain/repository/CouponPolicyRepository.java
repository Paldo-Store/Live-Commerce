package com.live_commerce.coupon.domain.repository;

import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.presentation.dto.response.CouponPolicySearchResult;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, String> {

  Optional<CouponPolicy> findByCodeAndDeletedStatusFalse(String code);

  Boolean existsByCodeAndDeletedStatusFalse(String code);

  List<CouponPolicy> findByDeletedStatusFalse();

  Boolean existsByCode(String code);

  @Query("SELECT new com.live_commerce.coupon.presentation.dto.response.CouponPolicySearchResult(" +
      "c.code, c.name, c.discountType, c.discountValue, c.minOrderAmt, c.maxOrderAmt, c.startAt, c.endAt, c.isActive) " +
      "FROM CouponPolicy c " +
      "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "AND (c.discountType = :discountType OR :discountType IS NULL) " +
      "AND (c.endAt > CURRENT_TIMESTAMP) " +
      "ORDER BY c.endAt ASC")
  Page<CouponPolicySearchResult> searchCouponPolicy(
      @Param("keyword") String keyword,
      @Param("discountType") DISCOUNT_TYPE discountType,
      Pageable pageable
  );
}