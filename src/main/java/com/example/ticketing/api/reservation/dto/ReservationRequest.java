package com.example.ticketing.api.reservation.dto;

import com.example.ticketing.api.ticket.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReservationRequest {
    long contentId;
    long seatId;
    long userId;
    ReservationRequest(long contentId, long seatId,long userId){
        this.seatId = seatId;
        this.userId = userId;
    }
}
