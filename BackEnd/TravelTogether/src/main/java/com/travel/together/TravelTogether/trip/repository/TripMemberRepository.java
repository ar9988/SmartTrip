package com.travel.together.TravelTogether.trip.repository;


import com.travel.together.TravelTogether.trip.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Integer> {

    @Query(value =
            "SELECT trip_id, COUNT(*) as member_count " +
                    "FROM trip_member " +
                    "GROUP BY trip_id",
            nativeQuery = true)
    List<Object[]> countMembersByTripId();
    List<TripMember> findByTripId(Integer tripId);
    boolean existsByTripIdAndUserId(Integer tripId, Integer userId);

    Integer countByTripId(Integer id);

    void deleteByTripId(Integer tripId);
}