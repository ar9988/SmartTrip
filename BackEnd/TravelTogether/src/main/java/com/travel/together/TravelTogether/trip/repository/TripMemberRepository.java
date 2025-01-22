package com.travel.together.TravelTogether.trip.repository;

import com.travel.together.TravelTogether.trip.dto.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {
    List<TripMember> findByTripId(Long tripId);
    boolean existsByTripIdAndUserId(Long tripId, Long userId);
}