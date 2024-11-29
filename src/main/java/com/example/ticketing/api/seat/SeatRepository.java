package com.example.ticketing.api.seat;

import com.example.ticketing.api.contents.Contents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByContents_Id(Long contentsId);
}
