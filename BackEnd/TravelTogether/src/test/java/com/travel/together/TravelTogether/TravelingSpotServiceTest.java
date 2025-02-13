package com.travel.together.TravelTogether;

import com.travel.together.TravelTogether.aiPlanning.service.TravelingSpotService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
public class TravelingSpotServiceTest {

    @Autowired
    private TravelingSpotService travelingSpotService;

    @Test
    @Transactional
    @Rollback(value = false) // 실제로 DB에 저장
    public void testSaveDataFromJsonFile() {
        String jsonFilePath = "src/main/resources/travelingSpot.json"; // JSON 파일 경로
        travelingSpotService.saveDataFromJsonFile(jsonFilePath);
        System.out.println("✅ JSON 데이터가 DB에 저장되었습니다!");
    }
}