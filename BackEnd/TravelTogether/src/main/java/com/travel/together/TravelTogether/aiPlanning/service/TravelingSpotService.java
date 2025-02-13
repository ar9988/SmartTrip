package com.travel.together.TravelTogether.aiPlanning.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.aiPlanning.dto.TravelingSpotDto;
import com.travel.together.TravelTogether.aiPlanning.entity.TravelingSpot;
import com.travel.together.TravelTogether.aiPlanning.repository.TravelingSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelingSpotService {
    private final TravelingSpotRepository travelingSpotRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용


    // JSON 파일에서 데이터를 읽어와 DB에 저장하는 메서드
    public void saveDataFromJsonFile(String filePath) {
        try {
            // 1. JSON 파일을 읽어서 DTO 리스트로 변환
            List<TravelingSpotDto> spotDtoList = objectMapper.readValue(
                    new File(filePath), new TypeReference<List<TravelingSpotDto>>() {}
            );

            // 2. DTO 리스트 → Entity 리스트 변환 후 DB 저장
            List<TravelingSpot> travelingSpots = spotDtoList.stream()
                    .map(dto -> TravelingSpot.builder()
                            .region(dto.getRegion())
                            .regionNumber(dto.getRegionNumber())
                            .contentid(dto.getContentid())
                            .title(dto.getTitle()) // place_name -> title
                            .overview(null)
                            .address(dto.getAddress()) // addr1 -> address
                            .imageurl(dto.getImageurl()) // firstimage -> imageurl
                            .build())
                    .collect(Collectors.toList());

            travelingSpotRepository.saveAll(travelingSpots);
            System.out.println("✅ JSON 데이터가 성공적으로 저장되었습니다!");

        } catch (IOException e) {
            System.err.println("❌ JSON 파일 읽기 오류: " + e.getMessage());
        }
    }


}
