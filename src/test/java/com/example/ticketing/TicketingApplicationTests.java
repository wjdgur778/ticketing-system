//package com.example.ticketing;
//
//import com.example.ticketing.api.contents.Contents;
//import com.example.ticketing.api.contents.ContentsRepository;
//import com.example.ticketing.api.contents.ContentsService;
//import com.example.ticketing.api.reservation.ReservationService;
//import com.example.ticketing.api.reservation.dto.ReservationResponse;
//import com.example.ticketing.api.seat.Seat;
//import com.example.ticketing.api.seat.SeatRepository;
//import com.example.ticketing.api.seat.SeatService;
//import com.example.ticketing.api.seat.dto.SeatResponse;
//import com.example.ticketing.api.user.UserService;
//import com.example.ticketing.api.user.dto.SignUpRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Disabled
//class TicketingApplicationTests {
//	@Autowired
//	private SeatService seatService;
//
//	@Autowired
//	private ContentsRepository contentsRepository;
//	@Autowired
//	private SeatRepository seatRepository;
//	@Autowired
//	private ContentsService contentsService;
//
//	@Autowired
//	private ReservationService reservationService;
//
//	@Autowired
//	private UserService userService;
//	private static final int THREAD_COUNT = 100;
//	private static final Long CONTENTS_ID = 1L; // 테스트용 Contents ID
//	private static Queue<Long> waitingQueue; // 대기 큐
//
//	@Transactional
//	@Test
//	public void test() throws InterruptedException{
//		Random random = new Random();
//
////		// 초기 데이터 삽입
////		for (int ii = 0; ii <10 ; ii++) {
////			Contents contents = new Contents((ii+1)+"번 컨탠츠",(ii+1)+"번 컨탠츠");
////			contents = contentsRepository.save(contents);
////			Seat seat1 = null;
////			for (int i = 0; i <10000 ; i++) {
////				seat1 = Seat.builder()
////						.contents(contents)
////						.isAvailable(true)
////						.number(i+1)
////						.build();
////				seatRepository.save(seat1);
////			}
////		}
//
//		//redis사용 여부를 알아보기 위한 2번의 조회
////		List<SeatResponse> list = seatService.getSeats(34l);
//////		Collections.sort(list,(i,j)->Long.compare(i.getNumber(),j.getNumber()));
////		list.forEach(
////				seatResponse -> System.out.println(seatResponse.getSeatId())
////		);
////
////		list = seatService.getSeats(34l);
//////		Collections.sort(list,(i,j)->Long.compare(i.getNumber(),j.getNumber()));
////		list.forEach(
////				seatResponse -> System.out.println(seatResponse.getSeatId())
////		);
//
////		for (int i = 300; i <320 ; i++) {
////			reservationService.reserveSeat(Integer.toUnsignedLong(i),Integer.toUnsignedLong(i));
////		}
//		waitingQueue = new LinkedList<>();
//
//		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
//		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
//
//		AtomicInteger successfulReservations = new AtomicInteger(0);
//		AtomicInteger failedReservations = new AtomicInteger(0);
//
//		List<Long> ticketIssuedOrder = Collections.synchronizedList(new ArrayList<>());
//		for (int i = 20; i < THREAD_COUNT+20; i++) {
//			final Long userId = (long) i;
//			waitingQueue.add(userId);
//			executorService.execute(() -> {
//				try {
//					// 예약 요청
//					//id가 500 ~ 510 번 (10개)의 좌석을 100개의 쓰래드가 점유하려한다.
//					ReservationResponse reservationResponse = reservationService.reserveSeat(random.nextLong(11)+500 , userId);
//					if (reservationResponse!=null) {
//						ticketIssuedOrder.add(userId);
//						successfulReservations.incrementAndGet();
//					} else {
//						failedReservations.incrementAndGet();
//					}
//				} catch (Exception e) {
//					failedReservations.incrementAndGet();
//				} finally {
//					latch.countDown(); // 작업 완료
//				}
//			});
//		}
//		/**
//		 */
//
//		latch.await(); // 모든 작업이 끝날 때까지 대기
//		executorService.shutdown();
//		System.out.println(successfulReservations.get());
//		// 테스트 검증
//		assertThat(ticketIssuedOrder).containsExactlyElementsOf(waitingQueue); // 티켓 발급 가능한 수까지만 비교
//
//
//		assertThat(successfulReservations.get()).isLessThanOrEqualTo(10); // 좌석 10개 제한
//		assertThat(successfulReservations.get() + failedReservations.get()).isEqualTo(THREAD_COUNT);
//	}
//
//}
