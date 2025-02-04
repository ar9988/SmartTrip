package com.travel.together.TravelTogether.album.controller;

import com.travel.together.TravelTogether.album.dto.PhotoAlbumRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoAlbumResponseDto;
import com.travel.together.TravelTogether.album.dto.PhotoRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoResponseDto;
import com.travel.together.TravelTogether.album.service.PhotoAlbumService;
import com.travel.together.TravelTogether.album.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/albums")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;
    private final PhotoService photoService;

    public PhotoAlbumController(PhotoAlbumService photoAlbumService, PhotoService photoService) {
        this.photoAlbumService = photoAlbumService;
        this.photoService = photoService;
    }

//    // [POST] /albums
//    // 새로운 앨범을 생성
//    @PostMapping
//    public ResponseEntity<PhotoAlbumResponseDto> createAlbum(@RequestBody PhotoAlbumRequestDto requestDto) {
//        PhotoAlbumResponseDto albumResponse = photoAlbumService.createPhotoAlbum(requestDto);
//        return ResponseEntity.ok(albumResponse);
//    }

    // [GET] /albums
    // 전체 앨범 목록 조회
    @GetMapping
    public ResponseEntity<List<PhotoAlbumResponseDto>> getAllAlbums() {
        List<PhotoAlbumResponseDto> albumList = photoAlbumService.getAllAlbums();
        return ResponseEntity.ok(albumList);
    }

    // [GET] /albums/{albumId}/photos
    // 특정 앨범의 사진 조회
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<List<PhotoResponseDto>> getPhotosByAlbum(@PathVariable("albumId") int albumId) {
        List<PhotoResponseDto> photos = photoService.getPhotosByAlbumId(albumId);
        return ResponseEntity.ok(photos);
    }

    // [POST] /albums/{albumId}/photos
    // 특정 앨범에 사진 업로드
    @PostMapping("/{albumId}/photos")
    public ResponseEntity<PhotoResponseDto> uploadPhotoToAlbum(
            @PathVariable("albumId") int albumId,
            @RequestPart("photoData") PhotoRequestDto photoRequestDto,
            @RequestPart("file") MultipartFile file) {

        // 경로에서 받은 albumId를 DTO에 설정
        photoRequestDto.setAlbumId(albumId);
        PhotoResponseDto responseDto = photoService.uploadPhoto(photoRequestDto, file);
        return ResponseEntity.ok(responseDto);
    }

}