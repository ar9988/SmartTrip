package com.travel.together.TravelTogether.aiPlanning.repository;

import com.google.cloud.storage.Option;
import com.travel.together.TravelTogether.aiPlanning.entity.Favorite;
import com.travel.together.TravelTogether.aiPlanning.entity.TravelingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(Integer userId);
    Optional<Favorite> findByUserIdAndTravelingSpot(Integer userId, TravelingSpot travelingSpot);
}
