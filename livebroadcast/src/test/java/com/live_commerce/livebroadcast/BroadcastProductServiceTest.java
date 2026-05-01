package com.live_commerce.livebroadcast;

import com.live_commerce.livebroadcast.application.dto.request.BroadcastProductConnectDto;
import com.live_commerce.livebroadcast.application.dto.response.BroadcastProductResponseDto;
import com.live_commerce.livebroadcast.application.service.BroadcastProductService;
import com.live_commerce.livebroadcast.application.validation.LiveBroadcastValidator;
import com.live_commerce.livebroadcast.application.validation.PermissionValidator;
import com.live_commerce.livebroadcast.application.validation.ProductValidator;
import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
import com.live_commerce.livebroadcast.domain.repository.BroadcastProductRepository;
import com.live_commerce.livebroadcast.infrastructure.client.product.ExternalProductResponseDto;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BroadcastProductServiceTest {

    @Autowired
    @InjectMocks
    private BroadcastProductService broadcastProductService;

    @Mock
    private LiveBroadcastValidator liveBroadcastValidator;
    @Mock
    private PermissionValidator permissionValidator;
    @Mock
    private ProductValidator productValidator;
    @Mock
    private BroadcastProductRepository broadcastProductRepository;

    @Test
    void 방송과_상품_연결_정상_성공() {
        // given
        UUID broadcastId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        RequestUserDetails user = mockUser();

        LiveBroadcast broadcast = mock(LiveBroadcast.class);
        when(broadcast.getLiveBroadcastId()).thenReturn(broadcastId);
        when(broadcast.getCompanyId()).thenReturn(companyId);

        ExternalProductResponseDto productDto = new ExternalProductResponseDto(productId, companyId);

        when(liveBroadcastValidator.validateExists(broadcastId)).thenReturn(broadcast);
        when(productValidator.getValidProductOrThrow(productId)).thenReturn(productDto);
        doNothing().when(liveBroadcastValidator).validateNotConnected(broadcastId, productId);
        doNothing().when(permissionValidator).validateOwnerOrMaster(user, broadcastId);

        // when
        BroadcastProductResponseDto result = broadcastProductService.connectBroadcastProduct(
                broadcastId,
                new BroadcastProductConnectDto(productId),
                user
        );

        // then
        assertEquals(productId, result.productId());
        verify(broadcastProductRepository).save(any());
    }

    @Test
    void 방송과_상품_회사다르면_예외발생() {
        // given
        UUID broadcastId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID companyA = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();
        RequestUserDetails user = mockUser();

        LiveBroadcast broadcast = mock(LiveBroadcast.class);
        when(broadcast.getLiveBroadcastId()).thenReturn(broadcastId);
        when(broadcast.getCompanyId()).thenReturn(companyA);

        ExternalProductResponseDto productDto = new ExternalProductResponseDto(productId, companyB);

        when(liveBroadcastValidator.validateExists(broadcastId)).thenReturn(broadcast);
        when(productValidator.getValidProductOrThrow(productId)).thenReturn(productDto);
        lenient().doNothing().when(permissionValidator).validateOwnerOrMaster(user, broadcastId);

        // when & then
        assertThrows(
                LiveBroadcastException.class,
                () -> broadcastProductService.connectBroadcastProduct(
                        broadcastId,
                        new BroadcastProductConnectDto(productId),
                        user
                )
        );
    }

    // Mock userDetails
    private RequestUserDetails mockUser() {
        return new RequestUserDetails(
                UUID.randomUUID(),
                "tester",
                List.of(new SimpleGrantedAuthority("ROLE_SHOW_HOST"))
        );
    }
}
