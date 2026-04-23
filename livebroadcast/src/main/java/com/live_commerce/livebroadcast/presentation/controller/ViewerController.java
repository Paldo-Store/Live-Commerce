package com.live_commerce.livebroadcast.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/livebroadcasts/viewers")
public class ViewerController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/{broadcastId}")
    public ResponseEntity<Integer> getViewerCount(@PathVariable String broadcastId) {
        Long count = redisTemplate.opsForSet().size("LIVE_VIEWERS:" + broadcastId);
        return ResponseEntity.ok(count != null ? count.intValue() : 0);
    }
}
