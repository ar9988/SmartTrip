package com.travel.together.TravelTogether.album.service;

import com.travel.together.TravelTogether.album.dto.PhotoAlbumRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoAlbumResponseDto;
import com.travel.together.TravelTogether.album.dto.PhotoResponseDto;
import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import com.travel.together.TravelTogether.album.entity.Photo;
import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.album.repository.PhotoRepository;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhotoAlbumService {

    private final PhotoAlbumRepository photoAlbumRepository;
    private final PhotoRepository photoRepository;
    private final TripRepository tripRepository;

    public PhotoAlbumService(PhotoAlbumRepository photoAlbumRepository,
                             PhotoRepository photoRepository,
                             TripRepository tripRepository) {
        this.photoAlbumRepository = photoAlbumRepository;
        this.photoRepository = photoRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public PhotoAlbumResponseDto createPhotoAlbum(PhotoAlbumRequestDto requestDto) {
        // 전달받은 tripId로 Trip 엔티티를 조회.
        Trip trip = tripRepository.findById(requestDto.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + requestDto.getTripId()));

        // PhotoAlbum 엔티티를 생성, 조회한 Trip과 기본 앨범 커버 이미지를 설정
        // default 값 넣어 놓음.
        PhotoAlbum album = new PhotoAlbum();
        album.setTrip(trip);
        album.setImageUrl("default.jpg");

        // 앨범을 데이터베이스에 저장.
        album = photoAlbumRepository.save(album);

        // 앨범의 정보를 DTO로 변환하여 반환.
        PhotoAlbumResponseDto responseDto = new PhotoAlbumResponseDto();
        responseDto.setId(album.getId());
        responseDto.setTripId(trip.getId());
        responseDto.setPhotos(List.of());
        return responseDto;
    }


    @Transactional(readOnly = true)
    public List<PhotoAlbumResponseDto> getAlbumsByTripId(int tripId) {
        // tripId의 앨범 목록 조회
        List<PhotoAlbum> albums = photoAlbumRepository.findByTripId(tripId);

        // 앨범에 속한 사진 목록을 DTO로 변환
        return albums.stream().map(album -> {
            PhotoAlbumResponseDto responseDto = new PhotoAlbumResponseDto();
            responseDto.setId(album.getId());
            responseDto.setTripId(album.getTrip().getId());

            //사진 조회
            List<Photo> photos = photoRepository.findByAlbumId(album.getId());
            List<PhotoResponseDto> photoDtos = photos.stream().map(photo -> {
                PhotoResponseDto dto = new PhotoResponseDto();
                dto.setAlbumId(photo.getAlbum().getId());
                dto.setUserId(photo.getUser().getId());
                dto.setLatitude(photo.getLatitude());
                dto.setLongitude(photo.getLongitude());
                dto.setFilePath(photo.getFilePath());
                dto.setTakenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null);
                return dto;
            }).collect(Collectors.toList());
            responseDto.setPhotos(photoDtos);
            return responseDto;
        }).collect(Collectors.toList());
    }
}
