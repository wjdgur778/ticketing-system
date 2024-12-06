package com.example.ticketing.api.reservation.dto;

import com.example.ticketing.api.ticket.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WaitingResponse {
    long rank;

    /**
     *  대기 중에 내가 몇번 째로 대기중인지 알기 위해 rank를 return 한다.
     */
    public WaitingResponse(long rank ) {
        this.rank = rank;
    }
}
