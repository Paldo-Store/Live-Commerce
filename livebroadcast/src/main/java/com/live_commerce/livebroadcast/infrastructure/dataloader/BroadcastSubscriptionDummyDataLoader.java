//package com.live_commerce.livebroadcast.infrastructure.dataloader;
//
//import com.live_commerce.livebroadcast.domain.model.BroadcastSubscription;
//import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
//import com.live_commerce.livebroadcast.domain.repository.BroadcastSubscriptionRepository;
//import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
//import com.live_commerce.livebroadcast.infrastructure.repository.JpaBroadcastSubscriptionRepository;
//import com.live_commerce.livebroadcast.infrastructure.repository.JpaLiveBroadcastRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
////@Profile("local")
//@Component
//@RequiredArgsConstructor
//public class BroadcastSubscriptionDummyDataLoader implements ApplicationRunner {
//
//    private final BroadcastSubscriptionRepository subscriptionRepository;
//    private final LiveBroadcastRepository liveBroadcastRepository;
//
//    @Override
//    @Transactional
//    public void run(ApplicationArguments args) {
//        if (subscriptionRepository.count() >= 10) return;
//
//        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000004");
//
//        UUID broadcastId = liveBroadcastRepository.findAll().stream()
//                .findFirst()
//                .map(LiveBroadcast::getLiveBroadcastId)
//                .orElseThrow(() -> new IllegalStateException("라이브 방송이 최소 1개는 필요합니다."));
//
//        BroadcastSubscription subscription = BroadcastSubscription.create(userId, broadcastId);
//
//        subscriptionRepository.save(subscription);
//    }
//}
//
