package com.example.ticketing.api.seat.dto;

import com.example.ticketing.api.seat.Seat;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SeatResponse {
    private long number;
    private boolean isAvailable;

    public SeatResponse(long number, boolean isAvailable) {
        this.number = number;
        this.isAvailable = isAvailable;
    }

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getNumber(), seat.isAvailable());
    }
}
