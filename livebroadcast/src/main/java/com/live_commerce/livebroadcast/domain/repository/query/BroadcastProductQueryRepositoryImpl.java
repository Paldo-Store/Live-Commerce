package com.live_commerce.livebroadcast.domain.repository.query;

import com.live_commerce.livebroadcast.domain.model.QBroadcastProduct;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class BroadcastProductQueryRepositoryImpl implements BroadcastProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UUID> findProductIdsByBroadcastId(UUID liveBroadcastId, Pageable pageable) {
        QBroadcastProduct bp = QBroadcastProduct.broadcastProduct;

        List<UUID> productIds = queryFactory
                .select(bp.productId)
                .from(bp)
                .where(
                        bp.liveBroadcastId.eq(liveBroadcastId),
                        bp.deletedStatus.isFalse()
                )
                .orderBy(getOrderSpecifier(pageable.getSort(), bp))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(bp.count())
                        .from(bp)
                        .where(
                                bp.liveBroadcastId.eq(liveBroadcastId),
                                bp.deletedStatus.isFalse()
                        )
                        .fetchOne()
        ).orElse(0L);


        return new PageImpl<>(productIds, pageable, total);
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort, QBroadcastProduct bp) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            PathBuilder<?> pathBuilder = new PathBuilder<>(bp.getType(), bp.getMetadata());

            String property = order.getProperty();
            if (property.equals("createdAt")) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                orders.add(new OrderSpecifier<>(direction, pathBuilder.get("createdAt", LocalDateTime.class)));
            }
        }

        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, bp.createdAt));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
