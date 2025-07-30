//package com.live_commerce.livebroadcast.infrastructure.dataloader;
//
//import com.live_commerce.livebroadcast.domain.model.BroadcastStatus;
//import com.live_commerce.livebroadcast.domain.model.LiveBroadcast;
//import com.live_commerce.livebroadcast.domain.repository.LiveBroadcastRepository;
//import com.live_commerce.livebroadcast.infrastructure.repository.JpaLiveBroadcastRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
////@Profile("local")
//@Component
//@RequiredArgsConstructor
//public class LiveBroadcastDummyDataLoader implements ApplicationRunner {
//
//    private final JpaLiveBroadcastRepository liveBroadcastRepository;
//
//    @Override
//    public void run(ApplicationArguments args) {
//
//        UUID fixedHostId = UUID.fromString("00000000-0000-0000-0000-000000000003");
//        UUID fixedCompanyId = UUID.fromString("196f6b15-e069-4b08-b736-9591fa2bcd7a");
//
//        List<LiveBroadcast> dummyList = IntStream.rangeClosed(1, 10)
//                .mapToObj(i -> {
//                    String name = "라이브 방송 " + i;
//                    LocalDateTime start = LocalDateTime.now().plusDays(i);
//                    LocalDateTime end = start.plusHours(1);
//
//                    return LiveBroadcast.create(name, start, end, fixedHostId, fixedCompanyId);
//                })
//                .collect(Collectors.toList());
//
//        liveBroadcastRepository.saveAll(dummyList);
//    }
//}
//
