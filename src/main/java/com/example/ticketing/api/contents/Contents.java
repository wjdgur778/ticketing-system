package com.example.ticketing.api.contents;

import com.example.ticketing.api.seat.Seat;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Contents {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String title;
    String description;

    @Builder
    public Contents(String title, String description){
        this.title = title;
        this.description = description;
    }
}
