package com.example.ticketing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
public class makeCsvFile {
    @Test
   public void abc(){
        final int maxsize = 500;
        final Random random=new Random();
        System.out.println("seatId,userId");
        for (long i = 300; i <300+maxsize ; i++) {
            //랜덤한 200개 좌석이 포함된 3000개의 요청 데이터
            System.out.println(random.nextLong(200)+800+","+i);
        }
   }
}
