package com.live_commerce.livebroadcast.domain.exception;

import com.live_commerce.livebroadcast.application.exception.CustomException;
import com.live_commerce.livebroadcast.application.exception.LiveBroadcastExceptionCode;

public class LiveBroadcastException extends CustomException {

    public LiveBroadcastException(LiveBroadcastExceptionCode code) {
        super(code);
    }

    public static LiveBroadcastException forLiveBroadcastNotFound() {
        throw new LiveBroadcastException(LiveBroadcastExceptionCode.NOT_FOUND);
    }


//    public static LiveBroadcastException forProductAlreadyConnectedToBroadcast() {
//        throw new LiveBroadcastException(LiveBroadcastExceptionCode.BROADCAST_PRODUCT_NOT_CONNECTED);
//    }

    public static LiveBroadcastException forProductAlreadyConnected() {
        throw new LiveBroadcastException(LiveBroadcastExceptionCode.BROADCAST_PRODUCT_ALREADY_CONNECTED);
    }

    public static LiveBroadcastException forExternalProductNotFound() {
        throw new LiveBroadcastException(LiveBroadcastExceptionCode.EXTERNAL_PRODUCT_NOT_FOUND);
    }
}
