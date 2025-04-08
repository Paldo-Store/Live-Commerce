package com.live_commerce.coupon.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.CouponPolicy;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import com.live_commerce.coupon.presentation.dto.response.ReadCouponPolicyResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CouponPolicyServiceTest {

  @Mock
  private CouponPolicyRepository couponPolicyRepository;

  @InjectMocks
  private CouponPolicyService couponPolicyService;

  private CreateCouponPolicyRequest request;

  private CouponPolicy couponPolicy;

  @BeforeEach
  void setUp() {
    request = new CreateCouponPolicyRequest(
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
        .code(UUID.randomUUID())
        .name("테스트 쿠폰")
        .discountType(DISCOUNT_TYPE.FIXED)
        .discountValue(BigDecimal.valueOf(100))
        .minOrderAmt(BigDecimal.valueOf(500))
        .maxOrderAmt(BigDecimal.valueOf(1000))
        .startAt(LocalDateTime.now().plusDays(1))
        .endAt(LocalDateTime.now().plusDays(30))
        .isActive(false)
        .build();

  }

  @Test
  @DisplayName("쿠폰 시작일이 종료일보다 클 경우 예외 발생")
  void throwExceptionForInvalidDateRange() {
    // given
    request = new CreateCouponPolicyRequest(
        request.name(),
        request.discountType(),
        request.discountValue(),
        request.minOrderAmt(),
        request.maxOrderAmt(),
        LocalDateTime.now().plusDays(10), // 시작일(startAt)이 종료일(endAt)보다 나중
        LocalDateTime.now().plusDays(5),
        request.isActive()
    );

    // when & then
    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("시작일은 종료일보다 이전이어야 합니다.");
  }

  @Test
  @DisplayName("고정 할인 금액이 최대 주문 금액보다 클 경우 예외 발생")
  void throwExceptionForDiscountGreaterThanMaxOrderAmt() {
    // given
    request = new CreateCouponPolicyRequest(
        request.name(),
        request.discountType(),
        BigDecimal.valueOf(2000), // 할인 금액을 최대 주문 금액보다 크게 설정
        request.minOrderAmt(),
        BigDecimal.valueOf(500),
        request.startAt(),
        request.endAt(),
        request.isActive()
    );
    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("할인 금액이 최대 주문 금액을 초과할 수 없습니다.");
  }

  @Test
  @DisplayName("정률 할인 금액이 100을 초과할 경우 예외 발생")
  void throwExceptionForRateDiscountGreaterThan100() {
    request = new CreateCouponPolicyRequest(
        request.name(),
        DISCOUNT_TYPE.RATE, // 할인 유형 RATE로 설정
        BigDecimal.valueOf(110), // 할인율이 100을 초과
        request.minOrderAmt(),
        request.maxOrderAmt(),
        request.startAt(),
        request.endAt(),
        request.isActive()
    );

    assertThatThrownBy(() -> couponPolicyService.createCouponPolicy(request))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("정률 할인 비율은 100을 넘을 수 없습니다.");
  }

  @Test
  @DisplayName("존재하는 쿠폰 정책 조회 성공")
  void getCouponPolicySuccess() {
    // given
    UUID validCouponId = couponPolicy.getCode();

    // when
    when(couponPolicyRepository.findByCodeAndDeletedStatusFalse(validCouponId)).thenReturn(
        Optional.of(couponPolicy));

    ReadCouponPolicyResponse response = couponPolicyService.getCouponPolicy(validCouponId);

    // then
    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(validCouponId);
    assertThat(response.name()).isEqualTo("테스트 쿠폰");
    verify(couponPolicyRepository, times(1)).findByCodeAndDeletedStatusFalse(validCouponId);

  }

  @Test
  @DisplayName("존재하지 않는 쿠폰 정책 조회 시 예외 발생")
  void getCouponPolicyNotFound() {
    // given
    UUID invalidCouponId = UUID.randomUUID();

    // when
    when(couponPolicyRepository.findByCodeAndDeletedStatusFalse(invalidCouponId)).thenReturn(
        Optional.empty());

    // then
    assertThatThrownBy(() -> couponPolicyService.getCouponPolicy(invalidCouponId))
        .isInstanceOf(CouponPolicyException.class)
        .hasMessageContaining("쿠폰 정책이 없거나 모두 삭제되었습니다.");

    verify(couponPolicyRepository, times(1)).findByCodeAndDeletedStatusFalse(invalidCouponId);

  }
}
