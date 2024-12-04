package com.example.ticketing.api.reservation.dto;

import com.example.ticketing.api.ticket.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationResponse {

    String status;
    int rank;
    int queueSize;
    Ticket ticket;

    public ReservationResponse(String status, int rank, int queueSize, Ticket ticket) {
        this.status = status;
        this.rank = rank;
        this.ticket = ticket;
        this.queueSize = queueSize;
    }
}
