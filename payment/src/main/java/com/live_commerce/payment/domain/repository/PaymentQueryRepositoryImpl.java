package com.live_commerce.payment.domain.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.live_commerce.payment.application.dto.request.PaymentSearchCondition;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.model.QPayment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentQueryRepositoryImpl implements PaymentQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Payment> searchPayment(PaymentSearchCondition condition, Pageable pageable) {
		QPayment payment = QPayment.payment;
		BooleanBuilder builder = new BooleanBuilder();

		// 삭제되지 않은 항목만 조회
		builder.and(payment.deletedStatus.isFalse());

		if (condition.userId() != null) {
			builder.and(payment.userId.eq(condition.userId()));
		}
		if (condition.orderId() != null) {
			builder.and(payment.orderId.eq(condition.orderId()));
		}
		if (condition.status() != null) {
			builder.and(payment.status.eq(condition.status()));
		}
		if (condition.createdAtFrom() != null) {
			builder.and(payment.createdAt.goe(condition.createdAtFrom()));
		}
		if (condition.createdAtTo() != null) {
			builder.and(payment.createdAt.loe(condition.createdAtTo()));
		}

		return queryFactory
			.selectFrom(payment)
			.where(builder)
			.orderBy(payment.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	@Override
	public long countPayment(PaymentSearchCondition condition) {
		QPayment payment = QPayment.payment;
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(payment.deletedStatus.isFalse());

		if (condition.userId() != null) {
			builder.and(payment.userId.eq(condition.userId()));
		}
		if (condition.orderId() != null) {
			builder.and(payment.orderId.eq(condition.orderId()));
		}
		if (condition.status() != null) {
			builder.and(payment.status.eq(condition.status()));
		}
		if (condition.createdAtFrom() != null) {
			builder.and(payment.createdAt.goe(condition.createdAtFrom()));
		}
		if (condition.createdAtTo() != null) {
			builder.and(payment.createdAt.loe(condition.createdAtTo()));
		}

		return queryFactory
			.select(payment.count())
			.from(payment)
			.where(builder)
			.fetchOne();
	}


}
