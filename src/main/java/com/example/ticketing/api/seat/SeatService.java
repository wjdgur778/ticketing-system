package com.example.ticketing.api.seat;

import com.example.ticketing.api.seat.dto.SeatResponse;
import com.example.ticketing.api.user.User;
import com.example.ticketing.config.redis.RedisConfig;
import com.example.ticketing.config.redis.RedisQueueManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {
    final private SeatRepository seatRepository;
    final private RedisQueueManager redisQueueManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SEAT_STATUS_KEY = "seat_status";
    /**
     * 콘텐츠별로 좌석 상태 조회
    */
    @Transactional
    public List<SeatResponse>getSeats(Long contentsId) {

        //1. redis에서 좌석 상태조회
        Map<Object, Object> seatStatuses = redisTemplate.opsForHash().entries(SEAT_STATUS_KEY+String.valueOf(contentsId));

        //2. redis에 데이터가 없으면 DB에서 조회 후 Redis 갱신
        if (seatStatuses.isEmpty()) {
            String key = SEAT_STATUS_KEY + String.valueOf(contentsId);
            log.info("Redis Key: " + key);
            log.info("redis에 데이터가 없습니다.");
            List<Seat> seats = seatRepository.findByContents_Id(contentsId);
            seats.forEach(seat ->
                    redisTemplate.opsForHash().put(SEAT_STATUS_KEY+String.valueOf(contentsId), seat.getNumber(), seat.isAvailable())
            );
            log.info("좌석 수 : " + seats.size() );

            return seats.stream()
                    .map(seat -> new SeatResponse(seat.getNumber(),seat.isAvailable()))
                    .collect(Collectors.toList());
        }

        log.info("redis에서 데이터를 응답합니다.");
        // 3. Redis 데이터를 기반으로 응답
        return seatStatuses.entrySet().stream()
                .map(entry -> new SeatResponse(
                        (Long)entry.getKey(),// Number
                        (Boolean)entry.getValue()// isAvailable
                ))
                .collect(Collectors.toList());
    }
//    public void reservationSeat(){
//
//    }
//    @Transactional
//    public void updateSeatStatus(Long seatId, boolean available) {
//        // 좌석 상태 변경
//        redisTemplate.opsForHash().put(SEAT_STATUS_KEY, seatId.toString(), available);
//
//        // DB도 업데이트
//        Seat seat = seatRepository.findById(seatId)
//                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
//        seat.setAvailable(available);
//        seatRepository.save(seat);
//    }

//    public SeatResponse saveSeats(){
//
//    }
}
