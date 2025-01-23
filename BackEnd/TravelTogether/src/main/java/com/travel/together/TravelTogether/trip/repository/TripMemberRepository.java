package com.travel.together.TravelTogether.trip.repository;


import com.travel.together.TravelTogether.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Integer> {
//    List<TripMember> findByTripId(Integer tripId);
//    boolean existsByTripIdAndUserId(Integer tripId, Integer userId);

    @Query("SELECT new map(t.id as tripId, COUNT(tm) as count) FROM TripMember tm RIGHT JOIN tm.trip t WHERE t.id IN :tripIds GROUP BY t.id")
    Map<Integer, Long> countByTripIdIn(@Param("tripIds") List<Integer> tripIds);

    boolean existsByTripIdAndUserId(Integer tripId, Integer userId);
    List<TripMember> findByTripId(Integer tripId);
}