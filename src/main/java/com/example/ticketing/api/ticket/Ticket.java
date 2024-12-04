package com.example.ticketing.api.ticket;

import com.example.ticketing.api.contents.Contents;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ticket", indexes = {
        @Index(name = "idx_uuid", columnList = "uuid"),
})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long seatId;
    private String uuid;//고유 번호
    @ManyToOne
    private Contents contents;

    @Builder
    public Ticket(Long seatId ,String uuid, Contents contents){
        this.seatId = seatId;
        this.uuid =uuid;
        this.contents = contents;
    }

}
