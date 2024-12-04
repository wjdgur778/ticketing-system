package com.example.ticketing.api.seat;

import com.example.ticketing.api.contents.Contents;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long number;
    private boolean isAvailable;
    @ManyToOne
    @JoinColumn(name = "contents_id")
    private Contents contents;

    @Builder
    public Seat (long id, long number, boolean isAvailable, Contents contents){
        this.id = id;
        this.number =number;
        this.isAvailable =isAvailable;
        this.contents = contents;
    }

}
