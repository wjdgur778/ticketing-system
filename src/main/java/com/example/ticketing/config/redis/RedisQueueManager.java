package com.example.ticketing.config.redis;

import com.example.ticketing.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/*
Redis의 List자료구조를 통해 대기열을 관리한다.
 */
@Component
@RequiredArgsConstructor
public class RedisQueueManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY = "reservationQueue";
    // 대기열에 사용자 추가 (score를 우선순위 또는 타임스탬프로 사용)

    public void addToQueue(String email) {
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(QUEUE_KEY, email, now);
    }
    // 대기열에서 첫 번째 사용자 가져오기 및 제거

    public String popFromQueue() {
        // 첫 번째 사용자 조회
        Set<String> users = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, 0);
        if (users == null || users.isEmpty()) {
            return null;
        }

        String firstUser = users.iterator().next();

        // 대기열에서 사용자 제거
        redisTemplate.opsForZSet().remove(QUEUE_KEY, firstUser);
        return firstUser;
    }

    // 대기열 크기 확인
    public Long getQueueSize() {
        return redisTemplate.opsForZSet().size(QUEUE_KEY);
    }

    // 대기열에 남은 사용자 조회
    public Set<String> getQueue() {
        return redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
    }
}