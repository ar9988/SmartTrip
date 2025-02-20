package com.travel.together.TravelTogether.firebase.repository;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.firebase.entity.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, String> {
    List<FCMToken> findByUser(User user);
}
