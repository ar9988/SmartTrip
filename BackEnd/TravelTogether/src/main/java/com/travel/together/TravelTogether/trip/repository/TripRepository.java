package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query("SELECT t FROM Trip t WHERE t.endDate >= CURRENT_TIMESTAMP")
    List<Trip> findActiveTripsByUserId(@Param("userId") Integer userId);

    @Query(value =
            "SELECT t.* FROM trip t " +
                    "INNER JOIN trip_member tm ON t.id = tm.trip_id " +
                    "WHERE tm.user_id = :userId",
            nativeQuery = true)
    List<Trip> findTripsByUserId(@Param("userId") Integer userId);


    @Query("SELECT t FROM Trip t " +
            "JOIN TripMember tm ON t.id = tm.trip.id " +
            "WHERE tm.user.id = :userId " +
            "AND (DATE(t.startDate) = CURRENT_DATE " +
            "OR t.startDate > CURRENT_DATE) " +
            "ORDER BY t.startDate ASC " +
            "LIMIT 1")
    Optional<Trip> findUpcomingTripByUserId(@Param("userId") Integer userId);

    Optional<Trip> findFirstByStartDateGreaterThanOrderByStartDateAsc(LocalDateTime now);
}

