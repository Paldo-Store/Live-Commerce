package com.live_commerce.chat.application.service;

import com.live_commerce.chat.application.dto.message.ChatRedisPayload;
import com.live_commerce.chat.application.dto.request.ChatAnalyzeRequestDto;
import com.live_commerce.chat.application.dto.request.ChatCreateRequest;
import com.live_commerce.chat.application.dto.response.ChatCreateResponse;
import com.live_commerce.chat.application.dto.response.ChatDeleteResponse;
import com.live_commerce.chat.application.dto.response.ChatGetResponse;
import com.live_commerce.chat.application.exception.ChatException;
import com.live_commerce.chat.domain.model.Chat;
import com.live_commerce.chat.domain.repository.ChatRepository;
import com.live_commerce.chat.infrastructure.repository.ChatQueryRepository;
import com.live_commerce.chat.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatQueryRepository chatQueryRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final RedissonClient redisson;
    private static final int MIN_CHAT_THRESHOLD = 20;


    //chat 생성 서비스
    @Transactional
    public ChatCreateResponse createChat(ChatCreateRequest request, UUID userId) {
        // Redis Subscriber가 이 메서드를 호출해 실제 저장함
        Chat chat = new Chat(userId, request.chatting(), request.liveBroadcastId(), request.messageType());
        Chat saved = chatRepository.save(chat);

        // Redisson 카운팅 로직
        RAtomicLong counter = redisson.getAtomicLong("chat:count:" + request.liveBroadcastId());
        long count = counter.incrementAndGet();
        counter.expire(Duration.ofMinutes(10));

        if (count >= MIN_CHAT_THRESHOLD) {
            RSet<String> activeBroadcastSet = redisson.getSet("chat:active");
            activeBroadcastSet.add(request.liveBroadcastId().toString());
            activeBroadcastSet.expire(Duration.ofMinutes(15));
        }

        return ChatCreateResponse.of(saved);
    }


    // chat 전체 채팅 조회
    @Transactional(readOnly = true)
    public ChatGetResponse getAllChats(int page, int size, String sort, RequestUserDetails userDetails) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        logger.info("User role: " + role);

        // 권한 검증: 관리자와 쇼호스트만 접근 허용
        if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SHOW_HOST")) {
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
    public ChatGetResponse getChatsByUserId(UUID userId, int page, int size, String sort, RequestUserDetails userDetails) {
        Pageable pageable = getPageable(page, size, sort);
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        // 권한 검증: 관리자와 쇼호스트만 접근 허용
        if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SHOW_HOST")) {
            throw new ChatException("권한이 없습니다. 관리자 또는 쇼호스트만 접근 가능합니다.");
        }

        return ChatGetResponse.of(chatQueryRepository.findAllByUserId(userId, pageable));
    }

    //chat 삭제 service
    @Transactional
    public ChatDeleteResponse deleteChat(UUID chatId, UUID userId, RequestUserDetails userDetails) {
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        //삭제할 채팅 들고오기
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatException("채팅이 존재하지 않습니다."));

        //고객인 경우에는 본인 채팅만 삭제 가능
        if (role.equals("ROLE_CUSTOMER") && !chat.getUserId().toString().equals(userId)) {
            throw new ChatException("해당 채팅에 대한 삭제 권한이 없습니다.");
        }

        //채팅 삭제
        chat.delete(userId.toString()); // 실제 삭제 대신 삭제 플래그만 true로 변경
        return ChatDeleteResponse.of(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatAnalyzeRequestDto> getChatsSince(UUID broadcastId, LocalDateTime sinceTime) {
        List<Chat> chats = chatRepository.findRecentChats(broadcastId, sinceTime);
        return chats.stream()
            .map(chat -> new ChatAnalyzeRequestDto(chat.getLiveBroadcastId(), chat.getChatting()))
            .toList();
    }

    private boolean hasMasterRole(RequestUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MASTER"));
    }


}
