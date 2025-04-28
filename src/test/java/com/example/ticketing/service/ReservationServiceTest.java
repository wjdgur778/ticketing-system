package com.example.ticketing.service;

import com.example.ticketing.api.contents.Contents;
import com.example.ticketing.api.reservation.ReservationService;
import com.example.ticketing.api.reservation.dto.ReservationResponse;
import com.example.ticketing.api.seat.Seat;
import com.example.ticketing.api.seat.SeatRepository;
import com.example.ticketing.api.ticket.Ticket;
import com.example.ticketing.api.ticket.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Disabled
class ReservationServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private ReservationService reservationService;

    private static final int TOTAL_USERS = 100;
    private static final int TOTAL_SEATS = 10;
    private static final String WAIT_QUEUE_KEY = "WAIT_QUEUE";
    private static final String WORKING_QUEUE_KEY = "WORKING_QUEUE";
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void testConcurrentSeatReservation() throws InterruptedException {
//        // Mock ValueOperations
//        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        when(valueOperations.increment(anyString())).thenReturn(1L);
//        // Mock 좌석 초기화
//        List<Seat> seats = new ArrayList<>();
//        for (long i = 1; i <= TOTAL_SEATS; i++) {
//            Seat seat = new Seat();
//            seat.setId(i);
//            seat.setAvailable(true);
//            seats.add(seat);
//
//            // Mock SeatRepository의 findById 동작 설정
//            when(seatRepository.findById(i)).thenReturn(Optional.of(seat));
//        }
//
//        // TicketService의 createTicket 동작 Mock
//        when(ticketService.createTicket(anyLong(), anyLong(), any()))
//                .thenAnswer(invocation -> {
//                    Long userId = invocation.getArgument(0);
//                    Long seatId = invocation.getArgument(1);
//                    Contents content = invocation.getArgument(2);
//                    return new Ticket(seatId, UUID.randomUUID().toString(), content);
//                });
//        // RedisTemplate Mock 설정
//        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
//        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
//        when(zSetOperations.size(anyString())).thenReturn(0L);
//        doAnswer(invocation -> null).when(zSetOperations).add(anyString(), anyString(), anyDouble());
//        doAnswer(invocation -> null).when(zSetOperations).remove(anyString(), anyString());
//
//        // 스레드 풀 생성
//        ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_USERS);
//        List<ReservationResponse> responses = new ArrayList<>();
//
//        // 100명의 사용자 요청
//        for (long userId = 1; userId <= TOTAL_USERS; userId++) {
//            long uid = userId;
//            executorService.submit(() -> {
//                try {
//                    ReservationResponse response = reservationService.reserveSeat((uid % TOTAL_SEATS) + 1, uid);
//                    synchronized (responses) {
//                        responses.add(response);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        // 스레드 종료 및 결과 대기
//        executorService.shutdown();
//        executorService.awaitTermination(1, TimeUnit.MINUTES);
//
//        // 성공 및 대기 응답 확인
//        long successCount = responses.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
//        long waitingCount = responses.stream().filter(r -> "WAITING".equals(r.getStatus())).count();
//
//        System.out.println("Success Count: " + successCount);
//        System.out.println("Waiting Count: " + waitingCount);
//
//        // 검증
//        assertThat(successCount).isEqualTo(TOTAL_SEATS); // 좌석 수만큼 성공해야 함
//        assertThat(waitingCount).isEqualTo(TOTAL_USERS - TOTAL_SEATS); // 나머지는 대기 상태여야 함

//        ---------------------------------------------------------------------------------


        // Mock ValueOperations
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Mock ZSetOperations
        ZSetOperations<String, Object> zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // Mock WORKING_QUEUE_KEY 초기화
        when(zSetOperations.size(WORKING_QUEUE_KEY)).thenReturn(1L);
        when(zSetOperations.range(WORKING_QUEUE_KEY, 0, 0))
                .thenReturn(Set.of("20")); // 특정 userId를 첫 번째로 설정

        when(zSetOperations.score(WORKING_QUEUE_KEY, "20")).thenReturn(1.0);

        // Mock 좌석 초기화
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setAvailable(true);
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Mock TicketService
        when(ticketService.createTicket(anyLong(), anyLong(), any()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    Long seatId = invocation.getArgument(1);
                    return new Ticket( seatId,UUID.randomUUID().toString(), new Contents());
                });

        // 사용자 요청 시도
//        ReservationResponse response = reservationService.reserveSeat(1L, 20L);

        // 검증
//        assertThat(response.getStatus()).isEqualTo("SUCESSS");
//        assertThat(response.getTicket()).isNotNull();
    }
}
