package com.live_commerce.coupon.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.live_commerce.coupon.domain.exception.CouponPolicyException;
import com.live_commerce.coupon.domain.model.DISCOUNT_TYPE;
import com.live_commerce.coupon.domain.repository.CouponPolicyRepository;
import com.live_commerce.coupon.presentation.dto.request.CreateCouponPolicyRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
}
