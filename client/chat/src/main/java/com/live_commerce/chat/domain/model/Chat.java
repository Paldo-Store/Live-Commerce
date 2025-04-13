package com.live_commerce.chat.domain.model;

import com.live_commerce.chat.infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_chat")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "chatting", nullable = false)
    private String chatting;

    //방송 id
    @Column(name="live_broadcast_id")
    private UUID liveBroadcastId;

    //메시지 타입
    @Enumerated(EnumType.STRING)
    private MessageType type;

    //채팅 생성
    public Chat(UUID userId, String chatting, UUID liveBroadcastId, MessageType type) {
        this.userId = userId;
        this.chatting = chatting;
        this.liveBroadcastId = liveBroadcastId;
        this.type = type;
    }
}
