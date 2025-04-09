package com.live_commerce.livebroadcast.domain.exception;

import com.live_commerce.livebroadcast.application.exception.CustomException;
import com.live_commerce.livebroadcast.application.exception.LiveBroadcastExceptionCode;

public class LiveBroadcastException extends CustomException {

    public LiveBroadcastException(LiveBroadcastExceptionCode code) {
        super(code);
    }

    public static void forLiveBroadcastNotFound() {
        throw new LiveBroadcastException(LiveBroadcastExceptionCode.NOT_FOUND);
    }



}
