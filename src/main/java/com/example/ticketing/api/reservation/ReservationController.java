package com.example.ticketing.api.reservation;

import com.example.ticketing.api.reservation.dto.ReservationRequest;
import com.example.ticketing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {
    final private ReservationService reservationService;

    @PostMapping("")
    ResponseEntity<Result> reservation(@RequestBody ReservationRequest reservationRequest){
        System.out.println(reservationRequest.getSeatId());
        return ResponseEntity.status(200)
                .body(Result.builder()
                        .message("success reservation")
                        .data(reservationService.reserveSeat(reservationRequest.getSeatId(), reservationRequest.getUserId()))
//                        .data(null)
                        .build()
                );
    }
}
