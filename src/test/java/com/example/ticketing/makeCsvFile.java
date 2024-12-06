package com.example.ticketing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
public class makeCsvFile {
    @Test
   public void abc(){
        final int maxsize = 100;
        final Random random=new Random();
        System.out.println("seatId,userId");
        for (long i = 300; i <300+maxsize ; i++) {
            System.out.println(random.nextLong(12)+700+","+i);
        }
   }
}
