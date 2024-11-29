package com.example.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketingApplication.class, args);
	}

}
