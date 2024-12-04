package com.example.ticketing.api.reservation;

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
    ResponseEntity<Result> reservation(Long seatId,Long contestsId){
        return ResponseEntity.status(200)
                .body(Result.builder()
                        .message("success reservation")
//                        .data(reservationService.reserveSeat(seatId,contestsId))
                        .data(null)
                        .build()
                );
    }
}
