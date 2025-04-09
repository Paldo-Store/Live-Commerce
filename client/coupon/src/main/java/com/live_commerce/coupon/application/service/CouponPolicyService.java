package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.application.validation.CouponPolicyValidator;
import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CouponPolicyService {

  private final CouponPolicyRepository couponRepository;
  private final CouponPolicyValidator couponPolicyValidator;

  public CreateCouponPolicyResponse createCouponPolicy(CreateCouponPolicyRequest request) {

    couponPolicyValidator.validateForCreatePolicy(request);

    CouponPolicy couponPolicy = request.toCouponPolicy();
    couponRepository.save(couponPolicy);
    return CreateCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  public ReadCouponPolicyResponse getCouponPolicy(UUID id) {
    CouponPolicy couponPolicy = findCouponPolicyOrElseThrow(id);
    return ReadCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  private CouponPolicy findCouponPolicyOrElseThrow(UUID id) {
    return couponRepository.findByCodeAndDeletedStatusFalse(id)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
  }

  public List<ReadCouponPolicyResponse> getCouponPolicies() {
    List<CouponPolicy> couponPolicyList = couponRepository.findByDeletedStatusFalse();
    return couponPolicyList.stream()
        .map(ReadCouponPolicyResponse::fromCouponPolicy)
        .collect(Collectors.toList());
  }

  public void deleteCouponPolicy(UUID id) {
    CouponPolicy couponPolicy = couponRepository.findById(id)
        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
    couponPolicy.markCouponAsDeleted(couponPolicy.getName());
    couponRepository.save(couponPolicy);
  }

  public void updateCouponPolicy(UUID id, UpdateCouponPolicyRequest request) {
    CouponPolicy updateCouponPolicy = findCouponPolicyOrElseThrow(id);
    couponPolicyValidator.validateForUpdatePolicy(request);
    updateCouponPolicy.updateCouponPolicy(request);
    couponRepository.save(updateCouponPolicy);
  }
}
