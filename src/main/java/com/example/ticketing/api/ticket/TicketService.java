package com.example.ticketing.api.ticket;

import com.example.ticketing.api.contents.Contents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public Ticket createTicket(Long userId, Long seatId, Contents contents) {
        String uuid = UUID.randomUUID().toString();
        log.info("createTicket 동작, UUID : " + uuid);

        Ticket ticket = Ticket.builder()
                .uuid(uuid)
                .seatId(seatId)
                .contents(contents)
                .build();

        log.info("Ticket ticket = new Ticket(userId,seatId,uuid.toString(),contents); 지나옴"+userId+" "+seatId+ " "+uuid+" "+contents.getId());

        ticketRepository.save(ticket);
        log.info("ticketRepository.save(ticket); 지나옴");

        return ticket;
    }
}
