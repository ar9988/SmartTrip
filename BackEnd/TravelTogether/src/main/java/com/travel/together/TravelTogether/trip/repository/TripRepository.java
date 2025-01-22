package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.dto.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query("SELECT t FROM Trip t JOIN FETCH t.creator WHERE t.id = :tripId")
    Optional<Trip> findByIdWithCreator(@Param("tripId") Long tripId);

    List<Trip> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
}