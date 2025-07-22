package com.live_commerce.coupon.presentation.dto.response;

import com.live_commerce.coupon.presentation.dto.request.CouponPolicySearchResult;
import java.util.List;
import org.springframework.data.domain.Page;

public record SearchCouponPolicyResponse(
    List<CouponPolicySearchResult> couponPolicies,
    int totalPages,
    int currentPage,
    long totalElements
){

  public static SearchCouponPolicyResponse fromCouponPolicyList(Page<CouponPolicySearchResult> couponPolicies) {
    return new SearchCouponPolicyResponse(
        couponPolicies.getContent(),
        couponPolicies.getTotalPages(),
        couponPolicies.getNumber() + 1,
        couponPolicies.getTotalElements()
    );
  }
}
