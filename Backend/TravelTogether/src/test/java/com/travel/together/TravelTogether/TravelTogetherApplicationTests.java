package com.travel.together.TravelTogether;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableAsync
@SpringBootTest
@EnableScheduling
@EnableJpaRepositories(basePackages = {
		"com.travel.together.TravelTogether.album.repository",
		"com.travel.together.TravelTogether.trip.repository",
		"com.travel.together.TravelTogether.auth.repository",

})
class TravelTogetherApplicationTests {

	@Test
	void contextLoads() {
	}

}
