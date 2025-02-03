package com.travel.together.TravelTogether.album.controller;

import com.travel.together.TravelTogether.album.dto.PhotoAlbumRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoAlbumResponseDto;
import com.travel.together.TravelTogether.album.service.PhotoAlbumService;
import com.travel.together.TravelTogether.trip.entity.Trip;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/albums")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;

    public PhotoAlbumController(PhotoAlbumService photoAlbumService) {
        this.photoAlbumService = photoAlbumService;
    }

    // [POST] /albums
    // 새로운 앨범을 생성
    @PostMapping
    public ResponseEntity<PhotoAlbumResponseDto> createAlbum(@RequestBody PhotoAlbumRequestDto requestDto) {
        PhotoAlbumResponseDto albumResponse = photoAlbumService.createPhotoAlbum(requestDto);
        return ResponseEntity.ok(albumResponse);
    }

    //[GET] /albums/trip/{tripId}
    // 여행의 앨범과 속한 사진 조회
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<PhotoAlbumResponseDto>> getAlbumsByTrip(@PathVariable int tripId) {
        List<PhotoAlbumResponseDto> albumList = photoAlbumService.getAlbumsByTripId(tripId);
        return ResponseEntity.ok(albumList);
    }
}