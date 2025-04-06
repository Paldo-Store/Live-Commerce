package com.live_commerce.livebroadcast;

import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateRequestDto;
import com.live_commerce.livebroadcast.application.dto.LiveBroadcastCreateResponseDto;
import com.live_commerce.livebroadcast.application.mapper.LiveBroadcastMapper;
import com.live_commerce.livebroadcast.application.service.LiveBroadcastService;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class LiveBroadcastServiceTest {

    @Mock
    private LiveBroadcastRepository liveBroadcastRepository;

    @InjectMocks
    private LiveBroadcastService liveBroadcastService;

    @Test
    void createBroadcast_success() {
        // given
        LiveBroadcastCreateRequestDto requestDto = LiveBroadcastCreateRequestDto.builder()
                .broadcastName("test 방송")
                .startTime(LocalDateTime.parse("2025-04-10T10:00:00"))
                .endTime(LocalDateTime.parse("2025-04-10T11:00:00"))
                .companyId(UUID.fromString("7f9c4424-5f93-4a4f-b2a6-f9c1a135b824"))
                .hostId(UUID.fromString("f3e08e72-9a0f-4ad8-8e49-b34672ec245c"))
                .build();

        LiveBroadcast mockEntity = LiveBroadcastMapper.createDtoToEntity(requestDto);

        // save가 호출되면 mockEntity를 반환하도록 설정
        Mockito.when(liveBroadcastRepository.save(Mockito.any(LiveBroadcast.class)))
                .thenReturn(mockEntity);

        // when
        LiveBroadcastCreateResponseDto responseDto = liveBroadcastService.createBroadcast(requestDto);

        // then
        assertNotNull(responseDto);
        assertEquals(requestDto.getBroadcastName(), responseDto.getBroadcastName());
        assertEquals(requestDto.getStartTime(), responseDto.getStartTime());

        // repository.save가 실제로 호출되었는지 검증
        Mockito.verify(liveBroadcastRepository, Mockito.times(1)).save(Mockito.any(LiveBroadcast.class));
    }
}
