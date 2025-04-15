package com.live_commerce.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = 1058139852L;

    public static final QOrder order = new QOrder("order1");

    public final com.live_commerce.order.presentation.common.QBaseEntity _super = new com.live_commerce.order.presentation.common.QBaseEntity(this);

    public final ComparablePath<java.util.UUID> broadcastId = createComparable("broadcastId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> couponId = createComparable("couponId", java.util.UUID.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final StringPath deletedBy = _super.deletedBy;

    //inherited
    public final BooleanPath deletedStatus = _super.deletedStatus;

    public final NumberPath<Long> finalPaidPrice = createNumber("finalPaidPrice", Long.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> productId = createComparable("productId", java.util.UUID.class);

    public final NumberPath<Integer> productQuantity = createNumber("productQuantity", Integer.class);

    public final NumberPath<Long> productTotalPrice = createNumber("productTotalPrice", Long.class);

    public final StringPath requirement = createString("requirement");

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final ComparablePath<java.util.UUID> userId = createComparable("userId", java.util.UUID.class);

    public QOrder(String variable) {
        super(Order.class, forVariable(variable));
    }

    public QOrder(Path<? extends Order> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrder(PathMetadata metadata) {
        super(Order.class, metadata);
    }

}

