package com.live_commerce.coupon.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.live_commerce.coupon.application.exception.CouponExceptionCode;
import com.live_commerce.coupon.application.validation.CouponPolicyValidator;
import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.request.UpdateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CouponPolicyServiceTest {

  @Mock
  private CouponPolicyRepository couponPolicyRepository;

  @Mock
  private CouponPolicyValidator couponPolicyValidator;  // couponPolicyValidator Mock 추가

  @InjectMocks
  private CouponPolicyService couponPolicyService;

  private CreateCouponPolicyRequest request;

  private CouponPolicy couponPolicy;

  @BeforeEach
  void setUp() {
    String code = "SUMMER_SALE_100";
    request = new CreateCouponPolicyRequest(
        code,
        "테스트 쿠폰",
        DISCOUNT_TYPE.FIXED,
        BigDecimal.valueOf(100),
        BigDecimal.valueOf(500),
        BigDecimal.valueOf(1000),
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(30),
        true
    );

    couponPolicy = CouponPolicy.builder()
        .code(code)
        .name("테스트 쿠폰")
        .discountType(DISCOUNT_TYPE.FIXED)
        .discountValue(BigDecimal.valueOf(100))
        .minOrderAmt(BigDecimal.valueOf(500))
        .maxOrderAmt(BigDecimal.valueOf(1000))
        .startAt(LocalDateTime.now().plusDays(1))
        .endAt(LocalDateTime.now().plusDays(30))
        .isActive(true)
        .build();

  }

  @Test
  @DisplayName("쿠폰 시작일이 종료일보다 클 경우 예외 발생")
  void throwExceptionForInvalidDateRange() {
    // given
    request = new CreateCouponPolicyRequest(
        request.code(),
        request.name(),
        request.discountType(),
        request.discountValue(),
        request.minOrderAmt(),
        request.maxOrderAmt(),
        LocalDateTime.now().plusDays(10),
        LocalDateTime.now().plusDays(5),
        request.isActive()
    );

    // when & then
    doThrow(new CouponPolicyException(CouponExceptionCode.INVALID_DATE_RANGE))
        .when(couponPolicyValidator).validateForCreatePolicy(
            any(CreateCouponPolicyRequest.class));  // void 메서드는 doThrow로 예외 던지기

    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("시작일은 종료일보다 이전이어야 합니다.");
  }

  @Test
  @DisplayName("고정 할인 금액이 최대 주문 금액보다 클 경우 예외 발생")
  void throwExceptionForDiscountGreaterThanMaxOrderAmt() {
    // given
    request = new CreateCouponPolicyRequest(
        request.code(),
        request.name(),
        request.discountType(),
        BigDecimal.valueOf(2000),
        request.minOrderAmt(),
        BigDecimal.valueOf(1000),
        request.startAt(),
        request.endAt(),
        request.isActive()
    );
    // when & then
    doThrow(new CouponPolicyException(CouponExceptionCode.DISCOUNT_GREATER_THAN_MAX_ORDER_AMOUNT))
        .when(couponPolicyValidator).validateForCreatePolicy(any(CreateCouponPolicyRequest.class));

    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("할인 금액이 최대 주문 금액을 초과할 수 없습니다.");
  }

  @Test
  @DisplayName("정률 할인 금액이 100을 초과할 경우 예외 발생")
  void throwExceptionForRateDiscountGreaterThan100() {
    request = new CreateCouponPolicyRequest(
        request.code(),
        request.name(),
        DISCOUNT_TYPE.RATE,
        BigDecimal.valueOf(110),
        request.minOrderAmt(),
        request.maxOrderAmt(),
        request.startAt(),
        request.endAt(),
        request.isActive()
    );
    // when & then
    doThrow(new CouponPolicyException(CouponExceptionCode.DISCOUNT_GREATER_THAN_100))
        .when(couponPolicyValidator).validateForCreatePolicy(any(CreateCouponPolicyRequest.class));

    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("정률 할인 비율은 100을 넘을 수 없습니다.");
  }

  @Test
  @DisplayName("존재하는 쿠폰 정책 조회 성공")
  void getCouponPolicySuccess() {
    // given
    String validCouponId = couponPolicy.getCode();

    // when
    when(couponPolicyRepository.findByCodeAndDeletedStatusFalse(validCouponId)).thenReturn(
        Optional.of(couponPolicy));

    ReadCouponPolicyResponse response = couponPolicyService.getCouponPolicy(validCouponId);

    // then
    assertThat(response).isNotNull();
    assertThat(response.code()).isEqualTo(validCouponId);
    verify(couponPolicyRepository, times(1)).findByCodeAndDeletedStatusFalse(validCouponId);

  }

  @Test
  @DisplayName("존재하지 않는 쿠폰 정책 조회 시 예외 발생")
  void getCouponPolicyNotFound() {
    // given
    String code = "WINTER_SALE_100";

    // when
    when(couponPolicyRepository.findByCodeAndDeletedStatusFalse(code)).thenReturn(
        Optional.empty());

    // then
    assertThatThrownBy(() -> couponPolicyService.getCouponPolicy(code))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("쿠폰 정책이 없거나 모두 삭제되었습니다.");

    verify(couponPolicyRepository, times(1)).findByCodeAndDeletedStatusFalse(code);

  }

  @Test
  @DisplayName("쿠폰 정책 삭제 시 소프트 delete 적용 확인")
  void deleteCouponPolicySoftDelete() {
    // given
    String validCouponId = couponPolicy.getCode();

    // when
    when(couponPolicyRepository.findById(validCouponId))
        .thenReturn(Optional.of(couponPolicy));

    couponPolicyService.deleteCouponPolicy(validCouponId);

    // then
    assertThat(couponPolicy.getDeletedStatus()).isTrue();
    assertThat(couponPolicy.getDeletedBy()).isNotNull();
    assertThat(couponPolicy.getDeletedAt()).isNotNull();

    when(couponPolicyRepository.findById(validCouponId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> couponPolicyService.getCouponPolicy(validCouponId))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("쿠폰 정책이 없거나 모두 삭제되었습니다.");

    verify(couponPolicyRepository, times(1)).save(couponPolicy);
  }

  @Test
  @DisplayName("쿠폰 정책 수정 성공")
  void updateCouponPolicySuccess() {
    // given
    String validCouponCode = couponPolicy.getCode();

    UpdateCouponPolicyRequest updateRequest = new UpdateCouponPolicyRequest(
        "수정된 쿠폰",
        DISCOUNT_TYPE.FIXED,
        BigDecimal.valueOf(200),
        BigDecimal.valueOf(500),
        BigDecimal.valueOf(1000),
        LocalDateTime.now().plusDays(2),
        LocalDateTime.now().plusDays(60),
        true
    );

    // when
    when(couponPolicyRepository.findByCodeAndDeletedStatusFalse(validCouponCode))
        .thenReturn(Optional.of(couponPolicy));
    when(couponPolicyRepository.save(couponPolicy)).thenReturn(couponPolicy);

    couponPolicyService.updateCouponPolicy(validCouponCode, updateRequest);

    // then
    assertThat(couponPolicy.getName()).isEqualTo("수정된 쿠폰");
    assertThat(couponPolicy.getDiscountValue()).isEqualTo(BigDecimal.valueOf(200));
    assertThat(couponPolicy.getStartAt()).isEqualTo(updateRequest.startAt());
    assertThat(couponPolicy.getEndAt()).isEqualTo(updateRequest.endAt());

    verify(couponPolicyRepository, times(1)).findByCodeAndDeletedStatusFalse(validCouponCode);
    verify(couponPolicyRepository, times(1)).save(couponPolicy);
  }


}
