package com.travel.together.TravelTogether.album.repository;

import com.travel.together.TravelTogether.album.entity.PhotoPlace;
import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PhotoPlaceRepository extends JpaRepository<PhotoPlace, Long> {
    Optional<PhotoPlace> findByNameAndAlbum(String name, PhotoAlbum album);
}
