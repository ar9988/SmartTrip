package com.travel.together.TravelTogether.trip.entity;

import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.util.SpringContext;
import jakarta.persistence.PostPersist;
import org.springframework.stereotype.Component;

@Component
public class TripEntityListener {

    /**
     * Trip 엔티티가 저장된 후 호출됩니다.
     * 여기서 PhotoAlbum을 생성한 후 PhotoAlbumRepository를 통해 DB에 저장합니다.
     */
    @PostPersist
    public void createPhotoAlbum(Trip trip) {
        // 새 PhotoAlbum 인스턴스 생성
        PhotoAlbum album = new PhotoAlbum();
        album.setTrip(trip);
        album.setImageUrl("default.jpg");  // 기본 앨범 커버 이미지 설정

        // SpringContext를 통해 PhotoAlbumRepository 빈을 가져온 후 앨범 저장
        PhotoAlbumRepository albumRepository = SpringContext.getBean(PhotoAlbumRepository.class);
        albumRepository.save(album);
    }
}
