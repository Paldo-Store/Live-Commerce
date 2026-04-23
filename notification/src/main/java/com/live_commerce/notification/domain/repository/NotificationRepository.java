package com.live_commerce.notification.domain.repository;

import com.live_commerce.notification.domain.model.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findAllByScheduledAtLessThanEqualAndIsSentFalse(LocalDateTime now);


  Optional<Notification> findByTargetIdAndDeletedStatusFalse(UUID targetId);

  boolean existsByTargetId(UUID uuid);
}
