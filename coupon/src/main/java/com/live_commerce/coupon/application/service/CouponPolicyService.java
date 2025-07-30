package com.live_commerce.coupon.application.service;

import com.live_commerce.coupon.application.validation.CouponPolicyValidator;
import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.infrastructure.security.RequestUserDetails;
import com.live_commerce.coupon.presentation.dto.request.CouponPolicySearchResult;
import com.live_commerce.coupon.presentation.dto.response.SearchCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.CreateCouponPolicyResponse;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CouponPolicyService {

  private final CouponPolicyRepository couponPolicyRepository;
  private final CouponPolicyValidator couponPolicyValidator;

  public CreateCouponPolicyResponse createCouponPolicy(CreateCouponPolicyRequest request,
      RequestUserDetails userDetails) {

    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }

    couponPolicyValidator.validateForCreatePolicy(request);

    CouponPolicy couponPolicy = request.toCouponPolicy();
    couponPolicyRepository.save(couponPolicy);
    return CreateCouponPolicyResponse.fromCouponPolicy(couponPolicy);
  }

  private boolean hasRoleMaster(RequestUserDetails userDetails) {
    return userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .noneMatch("ROLE_MASTER:"::equals);
  }

  @Transactional(readOnly = true)
  public ReadCouponPolicyResponse getCouponPolicy(String code, RequestUserDetails userDetails) {
    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }

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
  public List<ReadCouponPolicyResponse> getCouponPolicies(RequestUserDetails userDetails) {
    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }
    List<CouponPolicy> couponPolicyList = couponPolicyRepository.findByDeletedStatusFalse();
    return couponPolicyList.stream()
        .map(ReadCouponPolicyResponse::fromCouponPolicy)
        .collect(Collectors.toList());
  }

  public void deleteCouponPolicy(String code, RequestUserDetails userDetails) {
    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }

    CouponPolicy couponPolicy = couponPolicyRepository.findById(code)

        .orElseThrow(() -> {
          CouponPolicyException.forCouponPolicyNotFound();
          return null;
        });
    couponPolicy.markCouponAsDeleted(couponPolicy.getName());
    couponPolicyRepository.save(couponPolicy);
  }

  public void updateCouponPolicy(String code, UpdateCouponPolicyRequest request,
      RequestUserDetails userDetails) {
    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }

    CouponPolicy updateCouponPolicy = findCouponPolicyOrElseThrow(code);
    couponPolicyValidator.validateForUpdatePolicy(request);
    updateCouponPolicy.updateCouponPolicy(request);
    couponPolicyRepository.save(updateCouponPolicy);

  }

  public SearchCouponPolicyResponse searchCouponPolicy(String keyword, Integer page, String sortBy,
      DISCOUNT_TYPE discountType,
      RequestUserDetails userDetails) {

    if (!hasRoleMaster(userDetails)) {
      throw new IllegalIdentifierException("마스터 권한이 있는 유저만 생성할 수 있습니다.");
    }

    int pageSize = 10;
    int offset = (page - 1) * pageSize;

    Sort sort = sortBy.equalsIgnoreCase("asc") ? Sort.by(Sort.Order.asc("endAt"))
        : Sort.by(Sort.Order.desc("endAt"));
    PageRequest pageRequest = PageRequest.of(offset / pageSize, pageSize, sort);

    Page<CouponPolicySearchResult> pageResult = couponPolicyRepository.searchCouponPolicy(keyword,
        discountType, pageRequest);
    return SearchCouponPolicyResponse.fromCouponPolicyList(pageResult);
  }
}
