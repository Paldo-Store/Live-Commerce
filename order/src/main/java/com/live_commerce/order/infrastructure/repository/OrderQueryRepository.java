package com.live_commerce.order.infrastructure.repository;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.QOrder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;

    //주문 전체 조회
    public Page<Order> findAll(Pageable pageable) {
        List<Order> orders = queryFactory
                .selectFrom(order)
                .where(order.deletedAt.isNull()) // 삭제되지 않은 데이터만 조회
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.info("Fetched Orders: " + orders); // 이 로그가 중요

        long total = queryFactory
                .select(order.count())
                .from(order)
                .where(order.deletedAt.isNull())
                .fetchFirst();

        return new PageImpl<>(orders, pageable, total);
    }
}
