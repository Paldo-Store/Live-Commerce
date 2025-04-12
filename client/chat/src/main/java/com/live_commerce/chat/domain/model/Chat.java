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

    //채팅 생성
    public Chat(String userId, String chatting) {
        this.userId = UUID.fromString(userId);
        this.chatting = chatting;
    }
}
