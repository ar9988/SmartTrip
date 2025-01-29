package com.travel.together.TravelTogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
public class TravelTogetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelTogetherApplication.class, args);
	}

}
