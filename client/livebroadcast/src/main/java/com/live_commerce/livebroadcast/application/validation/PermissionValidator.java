package com.live_commerce.livebroadcast.application.validation;


import com.live_commerce.livebroadcast.domain.exception.LiveBroadcastException;
import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
import com.live_commerce.livebroadcast.infrastructure.security.RequestUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final LiveBroadcastRepository liveBroadcastRepository;

    /**
     * 해당 방송을 수정&삭제할 권한을 검증
     */
    public void validateOwnerOrMaster(RequestUserDetails user, UUID broadcastId) {
        if (user.isMaster()) return;

        UUID userId = user.getUserId();
        UUID hostId = liveBroadcastRepository.findHostIdByBroadcastId(broadcastId);

        if (!userId.equals(hostId)) {
            throw LiveBroadcastException.accessDenied();
        }
    }

    /**
     *  특정 방송과 특정 상품을 연결&해제할 수 있는 권한을 검증
     */
    public void validateBroadcastProductPermission(RequestUserDetails user, UUID broadcastId) {

    }

    /**
     *  MASTER 권한 사용자만 접근 가능하도록 검증
     */
    public void validateMasterOnly(RequestUserDetails user) {
        if (!user.isMaster()) {
            throw LiveBroadcastException.accessDenied();
        }
    }

}
