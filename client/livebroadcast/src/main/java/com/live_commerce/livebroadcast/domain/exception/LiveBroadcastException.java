package com.live_commerce.livebroadcast.domain.exception;

import com.live_commerce.livebroadcast.application.exception.CustomException;
import com.live_commerce.livebroadcast.application.exception.LiveBroadcastExceptionCode;

public class LiveBroadcastException extends CustomException {

    public LiveBroadcastException(LiveBroadcastExceptionCode code) {
        super(code);
    }

    public static LiveBroadcastException forLiveBroadcastNotFound() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.NOT_FOUND);
    }

    public static LiveBroadcastException forProductAlreadyConnected() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.BROADCAST_PRODUCT_ALREADY_CONNECTED);
    }

    public static LiveBroadcastException forExternalProductNotFound() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.EXTERNAL_PRODUCT_NOT_FOUND);
    }

    public static LiveBroadcastException forConnectedProductNotFound() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.PRODUCT_DISCONNECTED);
    }

    public static LiveBroadcastException forExternalCompanyNotFound() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.EXTERNAL_COMPANY_NOT_FOUND);
    }

    public static LiveBroadcastException forInvalidTimeRange() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.INVALID_TIME_RANGE);
    }

    public static LiveBroadcastException forUpdateFieldRequired() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.EXCEPTION_FIELD_REQUIRED);
    }

    public static LiveBroadcastException alreadySubscribed() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.ALREADY_SUBSCRIBED);
    }

    public static LiveBroadcastException forSubscriptionNotFound() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.SUBSCRIPTION_NOT_FOUND);
    }

    public static LiveBroadcastException forInvalidAlarmRequest() {
        return new LiveBroadcastException(LiveBroadcastExceptionCode.INVALID_ALARM_REQUEST);
    }
}
