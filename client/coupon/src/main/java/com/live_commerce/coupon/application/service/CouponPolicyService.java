package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.application.validation.CouponPolicyValidator;
import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CouponPolicyService {

  private final CouponPolicyRepository couponPolicyRepository;
  private final CouponPolicyValidator couponPolicyValidator;

  public CreateCouponPolicyResponse createCouponPolicy(CreateCouponPolicyRequest request) {

    couponPolicyValidator.validateForCreatePolicy(request);

    CouponPolicy couponPolicy = request.toCouponPolicy();
    couponPolicyRepository.save(couponPolicy);
    return CreateCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  @Transactional(readOnly = true)
  public ReadCouponPolicyResponse getCouponPolicy(String code) {
    CouponPolicy couponPolicy = findCouponPolicyOrElseThrow(code);
    return ReadCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  private CouponPolicy findCouponPolicyOrElseThrow(String code) {
    return couponPolicyRepository.findByCodeAndDeletedStatusFalse(code)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
  }

  @Transactional(readOnly = true)
  public List<ReadCouponPolicyResponse> getCouponPolicies() {
    List<CouponPolicy> couponPolicyList = couponPolicyRepository.findByDeletedStatusFalse();
    return couponPolicyList.stream()
        .map(ReadCouponPolicyResponse::fromCouponPolicy)
        .collect(Collectors.toList());
  }

  public void deleteCouponPolicy(String code) {
    CouponPolicy couponPolicy = couponPolicyRepository.findById(code)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
    couponPolicy.markCouponAsDeleted(couponPolicy.getName());
    couponPolicyRepository.save(couponPolicy);
  }

  public void updateCouponPolicy(String code, UpdateCouponPolicyRequest request) {
    CouponPolicy updateCouponPolicy = findCouponPolicyOrElseThrow(code);
    couponPolicyValidator.validateForUpdatePolicy(request);
    updateCouponPolicy.updateCouponPolicy(request);
    couponPolicyRepository.save(updateCouponPolicy);
  }
}
