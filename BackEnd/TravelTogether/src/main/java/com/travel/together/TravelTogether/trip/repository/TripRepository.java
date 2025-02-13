package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query(value =
            "SELECT t.* FROM trip t " +
                    "INNER JOIN trip_member tm ON t.id = tm.trip_id " +
                    "WHERE tm.user_id = :userId",
            nativeQuery = true)
    List<Trip> findTripsByUserId(@Param("userId") Integer userId);
}

