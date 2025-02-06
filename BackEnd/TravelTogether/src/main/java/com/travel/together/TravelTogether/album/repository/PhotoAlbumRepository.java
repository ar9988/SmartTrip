package com.travel.together.TravelTogether.album.repository;

import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Integer> {
    @Query("SELECT pa FROM PhotoAlbum pa " +
            "WHERE pa.trip.id IN " +
            "(SELECT tm.trip.id FROM TripMember tm WHERE tm.user.id = :userId)")
    List<PhotoAlbum> findAllByUserId(@Param("userId") Integer userId);
}
