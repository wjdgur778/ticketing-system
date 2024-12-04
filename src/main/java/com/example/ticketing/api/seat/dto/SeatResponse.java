package com.example.ticketing.api.seat.dto;

import com.example.ticketing.api.seat.Seat;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SeatResponse {
    private long seatId;
    private boolean isAvailable;

    public SeatResponse( long seatId, boolean isAvailable) {
        this.seatId = seatId;
        this.isAvailable = isAvailable;
    }

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.isAvailable());
    }
}
