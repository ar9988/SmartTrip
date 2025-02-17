package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DayRepository extends JpaRepository<Day, Integer> {
    List<Day> findByTripId(Integer tripId);


    List<Day> findByTripIdOrderByOrderNum(Integer tripId);

    Optional<Object> findFirstByTripIdOrderByIdDesc(Integer tripId);

    Optional<Day> findFirstByTripIdOrderByIdAsc(Integer tripId);

    List<Integer> findIdByTripIdOrderByIdAsc(Integer tripIdInt);

    @Query("SELECT d.id FROM Day d WHERE d.trip.id = :tripId ORDER BY d.id ASC")
    List<Integer> findIdByTripId(@Param("tripId") Integer tripId);

    void deleteByTripId(Integer tripId);
}



