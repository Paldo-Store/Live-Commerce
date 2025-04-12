package com.live_commerce.chat.application.service;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public ChatCreateResponse createChat(ChatCreateRequest request, String userId) {
        //TODO 권한 검증 추가 - 누구나 가능

        //chat 저장
        Chat chat = new Chat(userId, request.chatting());
        Chat saved = chatRepository.save(chat);
        return ChatCreateResponse.of(saved);
    }
}
