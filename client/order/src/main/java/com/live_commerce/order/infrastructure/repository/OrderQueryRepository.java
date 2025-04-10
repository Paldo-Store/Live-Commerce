package com.live_commerce.order.infrastructure.repository;

import com.live_commerce.order.domain.model.Order;
import com.live_commerce.order.domain.model.QOrder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;

    //주문 전체 조회
    public Page<Order> findAll(Pageable pageable) {
        List<Order> orders = queryFactory
                .selectFrom(order)
                .where(order.deletedStatus.eq(false)) // 삭제되지 않은 데이터만 조회
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(order.count())
                .from(order)
                .where(order.deletedAt.isNull())
                .fetchFirst();

        return new PageImpl<>(orders, pageable, total);
    }
}
