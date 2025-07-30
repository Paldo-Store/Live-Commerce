package com.live_commerce.chat.domain.repository;

import com.live_commerce.chat.domain.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
	@Query("SELECT c FROM Chat c WHERE c.liveBroadcastId = :broadcastId AND c.createdAt > :since")
	List<Chat> findRecentChats(@Param("broadcastId") UUID broadcastId, @Param("since") LocalDateTime since);}
