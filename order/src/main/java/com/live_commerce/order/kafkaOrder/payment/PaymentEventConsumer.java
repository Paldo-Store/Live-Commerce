package com.live_commerce.order.kafkaOrder.payment;

import com.live_commerce.order.application.service.OrderService;
import com.live_commerce.order.kafkaOrder.service.PaymentSuccessServiceKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentSuccessServiceKafka paymentSuccessServiceKafka;

    //payment -> order
    private static final String COMPLETED_TOPIC = "payment-completed";

    @KafkaListener(
            topics = COMPLETED_TOPIC,
            groupId = "order"
    )
    public void listenPaymentCompleted(PaymentCompletedEvent msg) {
        log.info("✅ 결제 성공 이벤트 수신(kafka): orderId={}, message={}", msg.orderId(), msg.message());
        paymentSuccessServiceKafka.updatePaymentSuccessKafka(msg);
    }
}
