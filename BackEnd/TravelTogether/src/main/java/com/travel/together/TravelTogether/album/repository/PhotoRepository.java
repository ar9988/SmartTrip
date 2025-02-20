package com.travel.together.TravelTogether.album.repository;

import com.travel.together.TravelTogether.album.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findByAlbumId(int albumId);
    void deleteByIdAndAlbumId(int photoId, int albumId);
}
