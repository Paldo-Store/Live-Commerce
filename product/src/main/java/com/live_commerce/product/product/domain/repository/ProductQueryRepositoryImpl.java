package com.live_commerce.product.product.domain.repository;


import com.live_commerce.product.product.application.dto.ProductSearchCondition;
import com.live_commerce.product.product.application.dto.SortOrder;
import com.live_commerce.product.product.domain.model.Product;
import com.live_commerce.product.product.domain.model.QProduct;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QProduct product = QProduct.product;

    @Override
    public Page<Product> search(ProductSearchCondition condition, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(containsKeyword(condition.keyword()))
                .orderBy(getSort(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long result = queryFactory
                .select(product.count())
                .from(product)
                .where(containsKeyword(condition.keyword()))
                .fetchOne();

        long total = result != null ? result : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression containsKeyword(String keyword) {
        return keyword == null ? null : product.name.containsIgnoreCase(keyword);
    }

    private OrderSpecifier<?> getSort(ProductSearchCondition condition) {
        PathBuilder<Product> path = new PathBuilder<>(Product.class, "product");

        String sortBy = condition.sortOrDefault().name();
        Order direction = condition.orderOrDefault() == SortOrder.asc ? Order.ASC : Order.DESC;

        return switch (sortBy) {
            case "price" -> new OrderSpecifier<>(direction, path.getNumber("price", Integer.class));
            case "name" -> new OrderSpecifier<>(direction, path.getString("name"));
            default -> new OrderSpecifier<>(direction, path.getDate("createdAt", LocalDateTime.class));
        };
    }

}
