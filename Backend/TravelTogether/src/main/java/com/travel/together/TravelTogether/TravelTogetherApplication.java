package com.travel.together.TravelTogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.travel.together.TravelTogether.album.repository",
		"com.travel.together.TravelTogether.trip.repository",
		"com.travel.together.TravelTogether.auth.repository",

})

public class TravelTogetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelTogetherApplication.class, args);
	}

}
