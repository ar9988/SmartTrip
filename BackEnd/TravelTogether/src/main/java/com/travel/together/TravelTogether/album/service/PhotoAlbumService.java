package com.travel.together.TravelTogether.album.service;

import com.travel.together.TravelTogether.album.dto.PhotoAlbumResponseDto;
import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import com.travel.together.TravelTogether.album.entity.Photo;
import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.album.repository.PhotoRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoAlbumService {

    private final PhotoAlbumRepository photoAlbumRepository;
    private final PhotoRepository photoRepository;

    public PhotoAlbumService(PhotoAlbumRepository photoAlbumRepository,
                             PhotoRepository photoRepository,
                             TripRepository tripRepository) {
        this.photoAlbumRepository = photoAlbumRepository;
        this.photoRepository = photoRepository;
    }


    @Transactional(readOnly = true)
    public List<PhotoAlbumResponseDto> getUserAlbums(Integer userId) {

        List<PhotoAlbum> albums = photoAlbumRepository.findAllByUserId(userId);

        return albums.stream().map(album -> {
            PhotoAlbumResponseDto responseDto = new PhotoAlbumResponseDto();
            responseDto.setId(album.getId());
            responseDto.setTripId(album.getTrip().getId());
            responseDto.setTripTitle(album.getTrip().getTitle());
            responseDto.setStartDate(album.getTrip().getStartDate().toString());
            responseDto.setEndDate(album.getTrip().getEndDate().toString());

            // 앨범에 속한 사진 조회
            List<Photo> photos = photoRepository.findByAlbumId(album.getId());
            if (!photos.isEmpty()) {
                responseDto.setRepImage(photos.get(0).getFilePath()); // 첫 번째 사진의 경로를 대표 이미지로 사용
            } else {
                responseDto.setRepImage(null); // 사진이 없을 경우 null 처리
            }
            return responseDto;
        }).collect(Collectors.toList());
    }
}
