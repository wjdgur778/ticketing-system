package com.example.ticketing.api.email;

import com.example.ticketing.api.ticket.Ticket;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailEvent {

    private long userId;
    private Ticket ticket;

    public EmailEvent(long userId,Ticket ticket){
        this.userId = userId;
        this.ticket = ticket;
    }
}
