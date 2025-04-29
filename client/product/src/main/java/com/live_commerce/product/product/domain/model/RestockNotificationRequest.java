//package com.live_commerce.product.product.domain.model;
//
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.UuidGenerator;
//
//import java.util.UUID;
//
//@Entity
//@Table(name = "p_restock_notification_requests",
//        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class RestockNotificationRequest extends BaseEntity {
//
//    @Id
//    @GeneratedValue(generator = "UUID")
//    @UuidGenerator
//    private UUID restockNotificationRequestId;
//
//    @Column(name = "user_id", nullable = false)
//    private UUID userId;
//
//    @Column(name = "product_id", nullable = false)
//    private UUID productId;
//
//    @Column(name = "notified", nullable = false)
//    private boolean notified = false;
//
//    private RestockNotificationRequest(UUID userId, UUID productId) {
//        this.userId = userId;
//        this.productId = productId;
//    }
//
//    public static RestockNotificationRequest create(UUID userId, UUID productId) {
//        return new RestockNotificationRequest(userId, productId);
//    }
//
//    public void markAsNotified() {
//        this.notified = true;
//    }
//}
//
