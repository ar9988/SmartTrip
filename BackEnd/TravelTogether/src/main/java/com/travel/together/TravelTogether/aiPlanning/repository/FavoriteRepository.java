package com.travel.together.TravelTogether.aiPlanning.repository;

import com.travel.together.TravelTogether.aiPlanning.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(Integer userId);
}
