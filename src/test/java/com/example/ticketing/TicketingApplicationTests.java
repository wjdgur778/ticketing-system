package com.example.ticketing;

import com.example.ticketing.api.contents.Contents;
import com.example.ticketing.api.contents.ContentsRepository;
import com.example.ticketing.api.contents.ContentsService;
import com.example.ticketing.api.seat.Seat;
import com.example.ticketing.api.seat.SeatRepository;
import com.example.ticketing.api.seat.SeatService;
import com.example.ticketing.api.seat.dto.SeatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TicketingApplicationTests {
	@Autowired
	private SeatService seatService;

	@Autowired
	private ContentsRepository contentsRepository;
	@Autowired
	private SeatRepository seatRepository;
	@Autowired
	private ContentsService contentsService;

//	@BeforeEach
//	public void setUp() {
//
//
//
//
//	}
//	@Transactional
	@Test
	public void test() {
		Random random = new Random();

//		// 초기 데이터 삽입
//		for (int ii = 0; ii <10 ; ii++) {
//			Contents contents = new Contents((ii+1)+"번 컨탠츠",(ii+1)+"번 컨탠츠");
//			contents = contentsRepository.save(contents);
//			Seat seat1 = null;
//			for (int i = 0; i <10000 ; i++) {
//				seat1 = Seat.builder()
//						.contents(contents)
//						.isAvailable(true)
//						.number(i+1)
//						.build();
//				seatRepository.save(seat1);
//			}
//		}

		//redis사용 여부를 알아보기 위한 2번의 조회
		List<SeatResponse> list = seatService.getSeats(34l);
		Collections.sort(list,(i,j)->Long.compare(i.getNumber(),j.getNumber()));
		list.forEach(
				seatResponse -> System.out.println(seatResponse.getNumber())
		);

		list = seatService.getSeats(34l);
		Collections.sort(list,(i,j)->Long.compare(i.getNumber(),j.getNumber()));
		list.forEach(
				seatResponse -> System.out.println(seatResponse.getNumber())
		);

	}

}
