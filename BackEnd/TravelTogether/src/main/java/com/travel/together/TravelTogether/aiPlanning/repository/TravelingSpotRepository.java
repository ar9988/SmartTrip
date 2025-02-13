package com.travel.together.TravelTogether.aiPlanning.repository;

import com.travel.together.TravelTogether.aiPlanning.entity.TravelingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelingSpotRepository extends JpaRepository<TravelingSpot, Integer> {
    List<TravelingSpot> findByRegionNumber(String regionNumber);
}
