package com.live_commerce.coupon.domain.model;

import com.live_commerce.coupon.domain.exception.CouponDiscountTypeException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Table(name = "p_coupon_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponPolicy extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID code;

  @Column(nullable = false, updatable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private DISCOUNT_TYPE discountType;

  @Column(nullable = false)
  private BigDecimal discountValue;

  @Column(nullable = false)
  private BigDecimal minOrderAmt;

  private BigDecimal maxOrderAmt;

  @Column(nullable = false)
  private LocalDateTime startAt;

  @Column(nullable = false)
  private LocalDateTime endAt;

  private boolean isActive;

  @Builder
  public CouponPolicy(UUID code, String name, DISCOUNT_TYPE discountType, BigDecimal discountValue,
      BigDecimal minOrderAmt, BigDecimal maxOrderAmt, LocalDateTime startAt, LocalDateTime endAt,
      boolean isActive) {

    if (name == null || discountType == null || discountValue == null
        || startAt == null || endAt == null) {
      throw new IllegalArgumentException("CouponPolicy의 필수 값이 null일 수 없습니다.");
    }

    this.code = code;
    this.name = name;
    this.discountType = discountType;
    this.discountValue = discountValue;
    this.minOrderAmt = minOrderAmt ;
    this.maxOrderAmt = maxOrderAmt;
    this.startAt = startAt;
    this.endAt = endAt;
    this.isActive = isActive;
  }

  public void validateDiscountType() {
    if (this.discountType == DISCOUNT_TYPE.FIXED && this.minOrderAmt == null) {
      CouponDiscountTypeException.forFixedDiscount();
    }

    if (this.discountType == DISCOUNT_TYPE.RATE && this.maxOrderAmt == null) {
      CouponDiscountTypeException.forRateDiscount();
    }

  }

  public void markCouponAsDeleted(String deletedBy){
    markAsDeleted(deletedBy);
  }

}