package com.travel.together.TravelTogether.album.repository;

import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Integer> {
    PhotoAlbum findByTrip_Id(int tripId);
}
