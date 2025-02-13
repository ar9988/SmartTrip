package com.travel.together.TravelTogether.aiPlanning.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.together.TravelTogether.aiPlanning.dto.TravelingSpotDto;
import com.travel.together.TravelTogether.aiPlanning.dto.TravelingSpotRegionDto;
import com.travel.together.TravelTogether.aiPlanning.entity.Favorite;
import com.travel.together.TravelTogether.aiPlanning.entity.TravelingSpot;
import com.travel.together.TravelTogether.aiPlanning.repository.FavoriteRepository;
import com.travel.together.TravelTogether.aiPlanning.repository.TravelingSpotRepository;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TravelingSpotService {
    private final TravelingSpotRepository travelingSpotRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;

    private TravelingSpotService(
            TravelingSpotRepository travelingSpotRepository,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            FavoriteRepository favoriteRepository) {
        this.travelingSpotRepository = travelingSpotRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
    }


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
                            .contentId(dto.getContentId())
                            .title(dto.getTitle()) // place_name -> title
                            .overView(null)
                            .address(dto.getAddress()) // addr1 -> address
                            .imageUrl(dto.getImageUrl()) // firstimage -> imageurl
                            .build())
                    .collect(Collectors.toList());

            travelingSpotRepository.saveAll(travelingSpots);
            System.out.println("✅ JSON 데이터가 성공적으로 저장되었습니다!");

        } catch (IOException e) {
            System.err.println("❌ JSON 파일 읽기 오류: " + e.getMessage());
        }
    }

    public List<TravelingSpotRegionDto> getRegionSpots(Integer regionId) {
        List<TravelingSpot> spots = travelingSpotRepository.findByRegionNumber(String.valueOf(regionId));
        return spots.stream().map(spot -> {
            TravelingSpotRegionDto dto = new TravelingSpotRegionDto();
            dto.setTitle(spot.getTitle());
            dto.setOverView(spot.getOverView());
            dto.setAddress(spot.getAddress());
            dto.setImageUrl(spot.getImageUrl());
            return dto;
    }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TravelingSpotDto> getUserFavoriteSpot(Integer userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream().map(favorite -> {
            TravelingSpot travelingSpot = favorite.getTravelingSpot();
            TravelingSpotDto dto = new TravelingSpotDto();
            dto.setRegion(travelingSpot.getRegion());
            dto.setRegionNumber(travelingSpot.getRegionNumber());
            dto.setContentId(travelingSpot.getContentId());
            dto.setTitle(travelingSpot.getTitle());
            dto.setOverView(travelingSpot.getOverView());
            dto.setAddress(travelingSpot.getAddress());
            dto.setImageUrl(travelingSpot.getImageUrl());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<TravelingSpotDto> updateFavoriteSpot() {

    }


}
