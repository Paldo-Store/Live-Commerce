package com.live_commerce.ai.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.live_commerce.ai.domain.model.AI;

public interface AiRepository extends JpaRepository<AI, UUID>, AiQueryRepository {
}