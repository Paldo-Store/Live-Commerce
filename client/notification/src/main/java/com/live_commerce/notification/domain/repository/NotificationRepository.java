package com.live_commerce.notification.domain.repository;

import com.live_commerce.notification.domain.model.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
