package com.live_commerce.chat.infrastructure.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    public enum MessageType {
        ENTER, CHAT, LEAVE
    }

    private MessageType type;
    private String roomId;
    private String sender;
    private String content;
}
