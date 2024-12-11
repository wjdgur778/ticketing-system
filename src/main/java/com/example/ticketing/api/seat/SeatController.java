package com.example.ticketing.api.seat;

import com.example.ticketing.common.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/seat")
public class SeatController {
    final private SeatService seatService;

    @GetMapping("/{contentsId}")
    public ResponseEntity<Result> getSeats(Long contentsId) {
        return ResponseEntity.status(200).body(Result.builder()
                .message("success getseats")
                .data(seatService.getSeats(contentsId))
                .build()
        );
    }

}
