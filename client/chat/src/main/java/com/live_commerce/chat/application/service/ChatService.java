package com.live_commerce.chat.application.service;

import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.dto.response.ChatDeleteResponse;
import com.live_commerce.chat.application.dto.response.ChatGetResponse;
import com.live_commerce.chat.application.exception.ChatException;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.repository.ChatRepository;
import com.live_commerce.chat.infrastructure.repository.ChatQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatQueryRepository chatQueryRepository;

    @Transactional
    public ChatCreateResponse createChat(ChatCreateRequest request, String userId) {
        //chat 저장
        Chat chat = new Chat(userId, request.chatting());
        Chat saved = chatRepository.save(chat);
        return ChatCreateResponse.of(saved);
    }

    // 전체 채팅 조회
    @Transactional(readOnly = true)
    public ChatGetResponse getAllChats(int page, int size, String sort, String role) {
        // 권한 검증: 관리자와 쇼호스트만 접근 허용
        if (!role.equals("MASTER") && !role.equals("SHOW_HOST")) {
            throw new ChatException("권한이 없습니다. 관리자 또는 쇼호스트만 접근 가능합니다.");
        }

        Pageable pageable = getPageable(page, size, sort);
        return ChatGetResponse.of(chatQueryRepository.findAll(pageable));
    }

    //페이징 함수
    private Pageable getPageable(final int page, final int size, final String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size); // 기본 정렬 없음
        }
        String[] sortParams = sort.split(",");
        List<Sort.Order> orders = new ArrayList<>();
        for (String param : sortParams) {
            String[] fieldAndDirection = param.trim().split("[- ]"); // '-' 또는 ' '으로 구분
            if (fieldAndDirection.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid sort parameter format. Expected 'field direction' (e.g., 'name asc').");
            }
            String field = fieldAndDirection[0].trim();
            String direction = fieldAndDirection[1].trim().toUpperCase();
            if (!direction.equals("ASC") && !direction.equals("DESC")) {
                throw new IllegalArgumentException("Invalid sort direction. Use 'asc' or 'desc'.");
            }
            Sort.Direction dir = Sort.Direction.fromString(direction);
            orders.add(new Sort.Order(dir, field));
        }
        Sort sortObj = Sort.by(orders);
        return PageRequest.of(page, size, sortObj);
    }

    //한 유저에 대한 채팅 조회
    @Transactional(readOnly = true)
    public ChatGetResponse getChatsByUserId(String userId, int page, int size, String sort, String role) {
        Pageable pageable = getPageable(page, size, sort);

        // 권한 검증: 관리자와 쇼호스트만 접근 허용
        if (!role.equals("MASTER") && !role.equals("SHOW_HOST")) {
            throw new ChatException("권한이 없습니다. 관리자 또는 쇼호스트만 접근 가능합니다.");
        }

        return ChatGetResponse.of(chatQueryRepository.findAllByUserId(UUID.fromString(userId), pageable));
    }

    //chat 삭제 service
    @Transactional
    public ChatDeleteResponse deleteChat(UUID chatId, String userId, String role) {
        //삭제할 채팅 들고오기
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatException("채팅이 존재하지 않습니다."));

        //고객인 경우에는 본인 채팅만 삭제 가능
        if (role.equals("CUSTOMER") && !chat.getUserId().toString().equals(userId)) {
            throw new ChatException("해당 채팅에 대한 삭제 권한이 없습니다.");
        }

        //채팅 삭제
        chat.delete(userId); // 실제 삭제 대신 삭제 플래그만 true로 변경
        return ChatDeleteResponse.of(chat);
    }
}
