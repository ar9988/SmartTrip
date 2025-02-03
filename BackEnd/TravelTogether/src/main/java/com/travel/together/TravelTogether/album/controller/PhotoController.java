package com.travel.together.TravelTogether.album.controller;

import com.travel.together.TravelTogether.album.dto.PhotoRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoResponseDto;
import com.travel.together.TravelTogether.album.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }


    //[POST] /photos/upload
    // 사진 업로드
    @PostMapping("/upload")
    public ResponseEntity<PhotoResponseDto> uploadPhoto(
            @ModelAttribute PhotoRequestDto photoRequestDto,
            @RequestParam("file") MultipartFile file) {
        PhotoResponseDto responseDto = photoService.uploadPhoto(photoRequestDto, file);
        return ResponseEntity.ok(responseDto);
    }

    // [GET] /photos/{albumId}
    // 앨범의 사진을 조회=
    @GetMapping("/{albumId}")
    public ResponseEntity<List<PhotoResponseDto>> getPhotosByAlbum(@PathVariable int albumId) {
        List<PhotoResponseDto> photos = photoService.getPhotosByAlbumId(albumId);
        return ResponseEntity.ok(photos);
    }
}
