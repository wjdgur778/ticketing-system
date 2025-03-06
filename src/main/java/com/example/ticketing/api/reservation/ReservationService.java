package com.example.ticketing.api.reservation;

import com.example.ticketing.api.contents.ContentsRepository;
import com.example.ticketing.api.email.EmailEvent;
import com.example.ticketing.api.reservation.dto.ReservationResponse;
import com.example.ticketing.api.seat.Seat;
import com.example.ticketing.api.seat.SeatRepository;
import com.example.ticketing.api.ticket.Ticket;
import com.example.ticketing.api.ticket.TicketService;
import com.example.ticketing.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static com.example.ticketing.common.exception.CommonErrorCode.*;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    //     Redis 대기열과 작업 큐에 관련된 키
    private static final String WAIT_QUEUE_KEY = "WAIT_QUEUE";
    private static final String WORKING_QUEUE_KEY = "WORKING_QUEUE";
    private static final int MAX_WAITING_QUEUE_SIZE = 100;
    private static final int MAX_WORKING_QUEUE_SIZE = 20;
    private static final int LOCK_TIMEOUT = 3000;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SeatRepository seatRepository;
    private final ContentsRepository contentsRepository;
    private final TicketService ticketService;
    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * 좌석id, userid.
     */
    public ReservationResponse reserveSeat(Long seatId, Long uid) {

//        //userId를 직접 파라미터로 받지 않고 securitycontext에서 얻어 사용
//        CustomUserDetail userDetail = (CustomUserDetail) SecurityContextHolder.getContext().getAuthentication().getDetails();
//        Long userId = userDetail.getUser().getId();
        Long userId = uid;

        // waiting queue와 working queue가 만들어질때만 만료시간 설정한다.
        if (!redisTemplate.hasKey(WAIT_QUEUE_KEY)) redisTemplate.expire(WAIT_QUEUE_KEY, Duration.ofMinutes(20));
        if (!redisTemplate.hasKey(WORKING_QUEUE_KEY)) redisTemplate.expire(WORKING_QUEUE_KEY, Duration.ofMinutes(10));

        // 1. 스케줄러로 인해 사용자가 작업큐에 들어갔는지 확인 후 들어갔다면, 티켓 발급
        if (redisTemplate.opsForZSet().score(WORKING_QUEUE_KEY, String.valueOf(userId)) != null) {
            log.info("스케줄러로 인해 사용자가 작업큐에 들어갔기에 예약시도");
            Ticket ticket = processReservation(userId, seatId);
            return new ReservationResponse("SUCESSS", 0, 0, ticket);
        }

        // 중복 추가 방지
        if (redisTemplate.opsForZSet().score(WAIT_QUEUE_KEY, String.valueOf(userId)) != null) {
            Long rank = redisTemplate.opsForZSet().rank(WAIT_QUEUE_KEY, String.valueOf(userId));
            return new ReservationResponse("WAITING", rank.intValue(), redisTemplate.opsForZSet().size(WAIT_QUEUE_KEY).intValue(), null);
        }

        // 1. 대기열에 추가 (wait 큐에 먼저 들어감)
        // 보다 정밀한 동시성 처리를 위한 score 계산
        Long order = redisTemplate.opsForValue().increment("queue_order");
        long ttlMillis = 10000; // 10초
        double score = System.nanoTime() + ttlMillis + (order / 1e6);

        log.info("WAIT_QUEUE에 진입 : score " + score);
        redisTemplate.opsForZSet().add(WAIT_QUEUE_KEY, String.valueOf(userId), score);

        // 2. 대기열 우선순위 확인
        Long rank = redisTemplate.opsForZSet().rank(WAIT_QUEUE_KEY, String.valueOf(userId));
        if (rank != null && (rank + 1) > MAX_WAITING_QUEUE_SIZE) { // 대기열 초과 시
            return new ReservationResponse("WAITING", rank.intValue() + 1, redisTemplate.opsForZSet().size(WAIT_QUEUE_KEY).intValue(), null);
        }

        log.info("대기열 우선순위에 들어 작업큐로 이동 시도 rank : " + rank);

        // 3. 작업 큐로 이동 시도
        if (tryMoveToWorkingQueue(userId, score)) {
            // 작업 큐로 이동하여 예약 처리
            log.info("작업큐로 이동 성공이후 예약 시도");
            Ticket ticket = processReservation(userId, seatId);
            log.info("작업큐로 이동 성공이후 예약 처리 완료 - ticket uuid : " + ticket.getUuid());
            return new ReservationResponse("SUCESSS", 0, 0, ticket);
        }

        //작업 큐에 들어가지 못한 경우
        // 대기 상태 유지 (working 큐에 들어갈 때까지 대기)
        return new ReservationResponse("WAITING", rank.intValue(), redisTemplate.opsForZSet().size(WAIT_QUEUE_KEY).intValue(), null);

    }

    private boolean tryMoveToWorkingQueue(Long userId, double score) {
        log.info("~~~~~~~~~~~~~~~~~~~tryMoveToWorkingQueue~~~~~~~~~~~~~~~~~~");
        // 현재 대기열에서 우선순위가 되면, 작업 큐로 이동
        Long workingQueueSize = redisTemplate.opsForZSet().size(WORKING_QUEUE_KEY);
        if (workingQueueSize != null && workingQueueSize < MAX_WORKING_QUEUE_SIZE) {
            redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, String.valueOf(userId));
            redisTemplate.opsForZSet().add(WORKING_QUEUE_KEY, String.valueOf(userId), score);
            return true;
        }
        return false;
    }

//    @Scheduled(fixedRate = 5000)
    public void removeExpiredValue() {
        redisTemplate.opsForZSet().removeRangeByScore(WAIT_QUEUE_KEY, 0, System.nanoTime());
    }

//    @Scheduled(fixedRate = 3000) // 1초마다 대기열
    public void processWorkingQueue() {
        log.info("~~~~~~~~~~~~~~~~~~~processWorkingQueue~~~~~~~~~~~~~~~~~~");
        // 작업 큐 크기 확인
        Long workingQueueSize = redisTemplate.opsForZSet().size(WORKING_QUEUE_KEY);
        if (workingQueueSize == null || workingQueueSize >= MAX_WORKING_QUEUE_SIZE) {
            return;
        }

        // 대기 큐에서 작업 큐로 이동 ( 최대 작업큐 크기 - 현재 작업큐 크기)
        Set<Object> candidates = redisTemplate.opsForZSet().range(WAIT_QUEUE_KEY, 0, MAX_WORKING_QUEUE_SIZE - workingQueueSize.intValue());
        if (candidates != null) {
            String user = null;
            for (Object userId : candidates) {
                user = String.valueOf(userId);
                double score = redisTemplate.opsForZSet().score(WAIT_QUEUE_KEY, user);
                redisTemplate.opsForZSet().remove(WAIT_QUEUE_KEY, user);
                redisTemplate.opsForZSet().add(WORKING_QUEUE_KEY, user, score);
            }
        }
    }

    /**
     * 서로 다른 사용자가 동일한 자원에 접근하여 한 좌석에 2명이 예약되는 (동시성 문제)가 발생할 수 있다.
     * redis을 통해 락을 거는 방식으로 예약 시도
     */
    @Transactional
    public Ticket processReservation(Long userId, Long seatId) {
        boolean lockReleased = false;
        String lockKey = "seat_lock:" + seatId;
        String lockValue = UUID.randomUUID().toString(); // 고유 값으로 락 구분
        try {
            // 1. 작업 큐에 해당 사용자가 존재하는지 확인
            if (redisTemplate.opsForZSet().score(WORKING_QUEUE_KEY, String.valueOf(userId)) == null) {
                throw new RestApiException(NOT_IN_WORKING_QUEUE);
            }

            // 2. 락 획득 (TTL 3분)
            // lock을 획득하되 다른 사람이 이미 락을 획득했다면 false를 반환

            acquireLock(lockKey, lockValue, LOCK_TIMEOUT);


            /**
             * ////////////////////
             * 3. 결제 관련 로직 구간
             * ////////////////////
             */
            Thread.sleep(3000);


            // 4. 좌석 예약 처리
            log.info("seatId : " + seatId + " 조회");
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RestApiException(NOT_FOUND));
            if (!seat.isAvailable()) {
                throw new RestApiException(NOT_AVAILABLE);
            }
            log.info("좌석 상태 변경");
            seat.setAvailable(false); // dirty check을 통해 seat에 add

            // 5. 티켓 발급
            log.info("티켓발급");
            Ticket ticket = ticketService.createTicket(userId, seatId, seat.getContents());

            // 6. 락 해제
            // 락을 즉시 해제하고 이메일을 전송
            releaseLock(lockKey, lockValue);


            // 7. 이메일 전송 (비동기 이벤트 방식)
            EmailEvent emailEvent = new EmailEvent(userId, ticket);
            applicationEventPublisher.publishEvent(emailEvent);
//            Thread.sleep(5000);//


            log.info("작업큐 제거");
            // 8. 작업 큐에서 제거
            redisTemplate.opsForZSet().remove(WORKING_QUEUE_KEY, String.valueOf(userId));

            return ticket;
        } catch (Exception e) {
            // 8. 장애시에 락 해제 (해당 사용자만 락 해제 가능하도록 값 비교)
            log.error("예약 중 예외 발생");
            throw new RestApiException(NOT_AVAILABLE);
        } finally {
            /**
             * 서로 다른 사용자가 같은 좌석을 예약하는 문제가 생김
             * 동시성 문제를 해결하기위해 lock을 사용해 해결하려고 시도했다.
             * 정상적으로 테스트가 진행되다가 또 다시 같은 문제가 생겨버렸다.
             * 문제 원인은 사용자 A가 로직을 수행하던 도중 소유한 락의 TTL이 만료되어 다른 사용자 B가 새롭게 락을 획득하게 되고,
             * 이후 사용자 A는 자신의 락이 유효하다고 생각해서 사용자 B와 동시에 예매 프로세스를 진행해 버리게 된다.
             * 이를 해결하기 위해서 TTL을 1분에서 3분으로 연장, 또한 TTL로 인해
             *
             * Redisson은 자동 연장 기능(Watchdog)으로 TTL을 자동 갱신 시킬 수도 있다.
             *
             * ttl을 연장할때는 사용자의 활동을 체크한 후에 ttl을 연장해야 한다.
             *
             * 콘서트 티켓팅 : 다른 사용자에게도 기회를 주어야 하기때문에 TTL을 짧게 설정
             * 빠른 결제 시스템: TTL을 짧게 설정하고 자동 연장 X
             * 오래 걸리는 결제 (예: 계좌이체): Watchdog 방식 or TTL 갱신 API
             * 일반적인 쇼핑몰: TTL을 적절히 설정 (예: 10분)
             *
             * TTL을 너무 짧게 설정하면 락이 풀려 다른 사용자가 개입할 위험이 있고,
             * 너무 길게 설정하면 불필요한 락 유지로 자원 낭비가 발생할 수 있음
             *
             * 적절한 TTL설정과 락 해제시 소유권 검증 필요
             */
            releaseLock(lockKey, lockValue);
        }
    }

    public void acquireLock(String lockKey, String lockValue, long timeout) {
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofMillis(timeout));
        log.info("lock여부 : " + lockAcquired);
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new RestApiException(LOCKED);
        }
    }

    public void releaseLock(String lockKey, String lockValue) {
        String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
        // 현재 락이 본인이 설정한 값과 같다면 삭제
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }

    }

}