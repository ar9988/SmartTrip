package com.travel.together.TravelTogether.aiPlanning.repository;

import com.travel.together.TravelTogether.aiPlanning.entity.Aiplanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiplanningRepository extends JpaRepository<Aiplanning, Integer> {
}
