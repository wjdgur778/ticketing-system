package com.example.ticketing.api.reservation;

import com.example.ticketing.api.contents.Contents;
import com.example.ticketing.api.contents.ContentsRepository;
import com.example.ticketing.api.reservation.dto.ReservationResponse;
import com.example.ticketing.api.seat.Seat;
import com.example.ticketing.api.seat.SeatRepository;
import com.example.ticketing.api.ticket.Ticket;
import com.example.ticketing.api.ticket.TicketService;
import com.example.ticketing.config.auth.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    //     Redis 대기열과 작업 큐에 관련된 키
    private static final String WAIT_QUEUE_KEY = "WAIT_QUEUE";
    private static final String WORKING_QUEUE_KEY = "WORKING_QUEUE";
    private static final int MAX_WAITING_QUEUE_SIZE = 10;
    private static final int MAX_WORKING_QUEUE_SIZE = 1;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SeatRepository seatRepository;
    private final ContentsRepository contentsRepository;
    private final TicketService ticketService;

    /**
     *
     */
    public ReservationResponse reserveSeat(Long seatId, Long uid) {

//        //userId를 직접 파라미터로 받지 않고 securitycontext에서 얻어 사용
//        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getDetails();
//        Long userId = userDetail.getUser().getId();

        Long userId = uid;

        // TTL 설정( 5분대기 )
        redisTemplate.expire(WAIT_QUEUE_KEY, Duration.ofMinutes(5));

        // 1. 스케줄러로 인해 사용자가 작업큐에 들어갔는지 확인 후 들어갔다면, 티켓 발급
        if (redisTemplate.opsForZSet().score(WORKING_QUEUE_KEY, String.valueOf(userId)) != null) {
            log.info("스케줄러로 인해 사용자가 작업큐에 들어갔기에 예약시도");

            Ticket ticket = processReservation(userId, seatId);
            return new ReservationResponse("SUCESSS", 0, 0, ticket);
        }

        // 중복 추가 방지
        if (redisTemplate.opsForZSet().score(WAIT_QUEUE_KEY, String.valueOf(userId)) != null) {
            throw new RuntimeException("이미 대기열에 존재합니다.");
        }

        // 1. 대기열에 추가 (wait 큐에 먼저 들어감)
        // 보다 정밀한 동시성 처리를 위한 score 계산
        Long order = redisTemplate.opsForValue().increment("queue_order");
        double score = System.currentTimeMillis() + (order / 1000000.0);
        log.info("WAIT_QUEUE에 진입 : score "+score);
        redisTemplate.opsForZSet().add(WAIT_QUEUE_KEY, String.valueOf(userId), score);

        // 2. 대기열 우선순위 확인
        Long rank = redisTemplate.opsForZSet().rank(WAIT_QUEUE_KEY, String.valueOf(userId));
        if (rank != null && rank > MAX_WAITING_QUEUE_SIZE) { // 대기열 초과 시
            return new ReservationResponse("WAITING", rank.intValue(), redisTemplate.opsForZSet().size(WAIT_QUEUE_KEY).intValue(), null);
        }

        log.info("대기열 우선순위에 들어 작업큐로 이동 시도 rank : "+rank);

        // 3. 작업 큐로 이동 시도
        if (tryMoveToWorkingQueue(userId, score)) {
            // 작업 큐로 이동하여 예약 처리
            log.info("작업큐로 이동 성공이후 예약 시도");

            Ticket ticket = processReservation(userId, seatId);
            log.info("작업큐로 이동 성공이후 예약 처리 완료 - ticket uuid : "+ticket.getUuid());
            return new ReservationResponse("SUCESSS", 0, 0, ticket);
        }

        //작업 큐에 들어가지 못한 경우
        // 대기 상태 유지 (working 큐에 들어갈 때까지 대기)
        return new ReservationResponse("WAITING", 0, 0, null);

    }

    private boolean tryMoveToWorkingQueue(Long userId, double score) {
        // 현재 대기열에서 우선순위가 되면, 작업 큐로 이동
        Long workingQueueSize = redisTemplate.opsForZSet().size(WORKING_QUEUE_KEY);
        if (workingQueueSize != null && workingQueueSize < MAX_WORKING_QUEUE_SIZE) {
            redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, String.valueOf(userId));
            redisTemplate.opsForZSet().add(WORKING_QUEUE_KEY, String.valueOf(userId), score);
            return true;
        }
        return false;
    }

    /**
     *
     */
    @Scheduled(fixedRate = 3000) // 3초마다 대기열
    public void processWorkingQueue() {
        // 작업 큐 크기 확인
        Long workingQueueSize = redisTemplate.opsForZSet().size(WORKING_QUEUE_KEY);
        if (workingQueueSize == null || workingQueueSize >= MAX_WORKING_QUEUE_SIZE) {
            return;
        }

        // 대기 큐에서 작업 큐로 이동 ( 최대 작업큐 크기 - 현재 작업큐 크기)
        Set<Object> candidates = redisTemplate.opsForZSet().range(WAIT_QUEUE_KEY, 0, MAX_WORKING_QUEUE_SIZE - workingQueueSize.intValue());
        if (candidates != null) {
            String user =null;
            for (Object userId : candidates) {
                user =  String.valueOf(userId);
                double score = redisTemplate.opsForZSet().score(WAIT_QUEUE_KEY, user);
                redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, user);
                redisTemplate.opsForZSet().add(WORKING_QUEUE_KEY, user, score);
            }
        }
    }

    /**
     * 서로 다른 사용자가 동일한 자원에 접근하여 한 좌석에 2명이 예약되는 (동시성 문제)가 발생할 수 있다.
     * redis의 분산락을 통한 예약 방식을 시도
     */
    @Transactional
    private Ticket processReservation(Long userId, Long seatId) {
        String lockKey = "seat_lock:" + seatId;
        String lockValue = UUID.randomUUID().toString(); // 고유 값으로 락 구분
        try {
            // 1. 분산 락 획득 (TTL 3분)
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofMinutes(3));
            if (lockAcquired == null || !lockAcquired) {
                throw new RuntimeException("현재 좌석이 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            /**
             * ////////////////////
             * 결제 관련 로직 구간
             *
             * ////////////////////
             *
             */

//            Thread.sleep();
            // 2. 좌석 예약 처리
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
            if (!seat.isAvailable()) {
                throw new RuntimeException("이미 예약된 좌석입니다.");
            }
            log.info("좌석 상태 변경");
            seat.setAvailable(false); // dirty check을 통해 seat에 add

            log.info("티켓발급");
            // 3. 티켓 발급
            Ticket ticket = ticketService.createTicket(userId, seatId,seat.getContents());

            // 4. 이메일 전송 (비동기 이벤트 방식)
//            emailService.sendTicketEmail(ticket);
            log.info("작업큐 제거");
            // 5. 작업 큐에서 제거
            redisTemplate.opsForZSet().remove(WORKING_QUEUE_KEY, String.valueOf(userId));

            return ticket;
        } finally {
            // 6. 락 해제 (해당 사용자만 락 해제 가능하도록 값 비교)
            String currentLockValue = (String)redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentLockValue)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

}