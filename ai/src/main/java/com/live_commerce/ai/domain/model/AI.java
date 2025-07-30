package com.live_commerce.ai.domain.model;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_ai")
public class AI extends BaseEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@Column(nullable = false)
	private UUID liveBroadcastId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String requestPayload;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String responsePayload;

	// 정적 팩토리 메서드
	public static AI of(UUID liveBroadcastId, String requestPayload, String responsePayload) {
		return new AI(liveBroadcastId, requestPayload, responsePayload);
	}

	// 프라이빗 생성자
	private AI(UUID liveBroadcastId, String requestPayload, String responsePayload) {
		this.liveBroadcastId = liveBroadcastId;
		this.requestPayload = requestPayload;
		this.responsePayload = responsePayload;
	}
}