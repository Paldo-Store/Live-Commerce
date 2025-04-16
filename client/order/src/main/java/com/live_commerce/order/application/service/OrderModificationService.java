package com.live_commerce.order.application.service;

import com.live_commerce.order.application.dto.request.OrderUpdateRequest;
import com.live_commerce.order.application.dto.response.OrderCreateResponse;
import com.live_commerce.order.application.dto.response.OrderProductResponse;
import com.live_commerce.order.application.dto.response.OrderUpdateResponse;
import com.live_commerce.order.application.exception.OrderException;
import com.live_commerce.order.application.exception.OrderExceptionCode;
import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.OrderStatus;
import com.live_commerce.order.domain.repository.OrderRepository;
import com.live_commerce.order.infrastructure.client.*;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckQuantityResponseDto;
import com.live_commerce.order.infrastructure.client.response.InventoryCheckResponseDto;
import com.live_commerce.order.presentation.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderModificationService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CouponClient couponClient;

    //주문 수정 service - 주문 상태 변경을 일어나지 않음.
    //주문 개수, 상품 id, 요청 사항, 쿠폰만 수정 가능
    @Transactional
    public OrderUpdateResponse updateCreator(UUID orderId, OrderUpdateRequest request, UUID userId, String role) {

        //수정은 방송 종료 후에도 가능
        //단, 상품 id는 변경 불가

        // orderId에 해당하는 주문 가져오기
        Order order  = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderException(OrderExceptionCode.NOT_FOUND));

        //고객일 경우 본인의 결제 내역만 수정 가능하게
        if ("ROLE_CUSTOMER".equals(role) && !order.getUserId().equals(userId)) {
            throw new OrderException("고객은 자신의 주문만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 현재 주문의 상태가 PENDING일떄만 수정 가능!
        OrderStatus status = order.getStatus();
        if (status != OrderStatus.PENDING) {
            throw new OrderException("주문 내역을 수정할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 상품 ID가 기존과 다를 경우 예외 발생 - 상품 id는 수정 불가. 수정하려면 주문 취소 후 다시 시도
        if (!order.getProductId().equals(request.productId())) {
            throw new OrderException("상품 ID는 수정할 수 없습니다. 만약 주문을 수정하고 싶다면 주문 취소 후 재주문해주세요.", HttpStatus.BAD_REQUEST);
        }

        // [productClient] 상품 쪽으로 검증 요청 필요 (상품의 개수랑 상품 ID를 같이 넘김)
        // 재고가 없거나 상품이 없다면 Exception
        //주문 요청 상품 id, update할 상품 주문 개수 -> product
        ApiResponse<OrderProductResponse> responseProduct = productClient.getProduct(request.productId()); //주문 요청 상품 id -> product
        OrderProductResponse productResponseByOrder = responseProduct.getData();

        // 재고 확인 로직
        ApiResponse<InventoryCheckResponseDto> responseInventory = inventoryClient.checkOrderableInventory(request.productId(), request.productQuantity());
        InventoryCheckResponseDto checkInventory = responseInventory.getData();
        if (!checkInventory.orderAvailable()) {
            throw new OrderException("재고가 없는 상태입니다.", HttpStatus.BAD_REQUEST);
        }
        log.info("재고 존재 여부 확인 완료");

        // TODO 이미 삭제(단종)된 상품일 경우
        // 상품이 품절일 경우
        if(productResponseByOrder.getSoldOut()){
            throw new OrderException("해당 상품은 품절된 상품입니다.", HttpStatus.BAD_REQUEST);
        }

        // 해당 상품의 남은 재고 수량 들고오기
        ApiResponse<InventoryCheckQuantityResponseDto> responseInventoryByQuantity = inventoryClient.checkInventoryQuantity(request.productId(), request.productQuantity());
        InventoryCheckQuantityResponseDto getInventoryQuantity = responseInventoryByQuantity.getData();


        int orderQty = request.productQuantity(); // 사용자가 주문한 상품의 수량 (요청 order -> product)
        log.info("재고 수량 들고오기");

        // 5. total 주문 금액 계산
        // 주문한 수량 * 주문한 상품 한 개의 가격
        Long productTotalPrice = (long) (orderQty * productResponseByOrder.getProductPrice());
        log.info("총 주문 금액 계산");

        // 6.  쿠폰에서 할인 적용할 금액 계산하도록 쿠폰아이디와 총 주문 금액(쿠폰 적용전) 넘겨주기
        // 최종 결제 금액 필드 선언
        Long finalPaidPrice = productTotalPrice;

        // 6-1. 지금 로그인 한 유저에 대한 쿠폰 목록 리스트들을 전부 들고온다.
        ApiResponse<IssuedCouponListResponse> responseCouponList = couponClient.getIssuedCoupons(userId);
        IssuedCouponListResponse couponListByUser = responseCouponList.getData();

        // 6-2. 쿠폰이 하나도 없다면 쿠폰 적용 없이 즉시 최종 결제 금액으로 반환 -> 원가로 결제
        // TODO 요청들어오는 couponId는 controller에서 비필수로 설정
        if( (couponListByUser == null) || (couponListByUser.coupons() == null)){
            Order updateOrder = request.toOrder(productTotalPrice, finalPaidPrice);
            order.updateOrder(updateOrder);
            OrderUpdateResponse.fromOrder(order);
        }

        // 6-3. 유저 목록 쿠폰이 있다면 -> 요청에서 들어온 couponId확인
        // 쿠폰id가 요청으로 들어오고 로그인한 사람의 쿠폰 목록들이 있다면 쿠폰 목록에서 요청 들어온 쿠폰 id 조회

        // 요청 couponId
        UUID requestCouponId = request.couponId();

        // 만약 요청 들어온 couponId가 없으면 원가 결제
        if (requestCouponId == null) {
            Order updateOrder = request.toOrder(productTotalPrice, finalPaidPrice);
            order.updateOrder(updateOrder);
            OrderUpdateResponse.fromOrder(order);
        }

        // 요청 들어온 couponId를 목록에서 찾고 그 일치하는 쿠폰 정보로 변수에 할당
        // 만약 요청 들어온 couponId가 쿠폰 목록에 없다면 해당 쿠폰이 목록에 없다는 예외 발생
        GetIssuedCouponResponse matchedCoupon = couponListByUser.coupons().stream()
                .filter(coupon -> coupon.id().equals(requestCouponId))
                .findFirst()
                .orElseThrow(() -> new OrderException("요청하신 쿠폰은 사용자의 보유 목록에 없습니다.", HttpStatus.BAD_REQUEST));

        // 7. 그 쿠폰에 해당하는 couponCode 찾기 (order -> coupon)

        // 쿠폰 id와 목록 쿠폰id과 일치하는 couponId 변수에 할당
        UUID matchedRequestCouponId = matchedCoupon.id();
        // couponCode 가져오기
        String requestCouponCode = matchedCoupon.couponCode();

        // 7-1. couponCode로 coupon policy에서 쿠폰 정책 조회 및 가져오기
        ApiResponse<ReadCouponPolicyResponse> responseCouponPolicy= couponClient.getCouponPolicy(requestCouponCode);
        ReadCouponPolicyResponse couponPolicyByCouponCode = responseCouponPolicy.getData();

        //8. 최종 결제 금액 계산

        //할인 타입 - fixed, rate
        String discountType = couponPolicyByCouponCode.discountType();
        // 할인률, 할인값
        Long discountValue = Long.parseLong(couponPolicyByCouponCode.discountValue());;

        //할인값으로 계산
        if(discountType.equalsIgnoreCase("fixed")){
            finalPaidPrice = productTotalPrice - discountValue;  //할인 고정값
        }

        //할인률로 계산
        if(discountType.equalsIgnoreCase("rate")){
            Long discountAmount = (productTotalPrice * discountValue) / 100;  //할인률로 계산
            finalPaidPrice = productTotalPrice - discountAmount;
        }
        log.info("할인 금액 적용한 최종 결제 예상 금액");

        //주문 수정
        Order updateOrder = request.toOrder(productTotalPrice, finalPaidPrice);
        order.updateOrder(updateOrder);

        return OrderUpdateResponse.fromOrder(order);
    }
}
