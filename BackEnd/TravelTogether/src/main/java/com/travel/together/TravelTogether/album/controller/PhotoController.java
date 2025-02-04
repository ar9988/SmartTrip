//package com.travel.together.TravelTogether.album.controller;
//
//import com.travel.together.TravelTogether.album.service.S3Service;
//import com.travel.together.TravelTogether.album.dto.BatchPhotoRequestDto;
//import com.travel.together.TravelTogether.album.dto.PhotoRequestDto;
//import com.travel.together.TravelTogether.album.dto.PhotoResponseDto;
//import com.travel.together.TravelTogether.album.service.PhotoService;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/photos")
//public class PhotoController {
//
//    private final PhotoService photoService;
//    private final S3Service s3Service;
//
//    public PhotoController(PhotoService photoService, S3Service s3Service) {
//        this.photoService = photoService;
//        this.s3Service = s3Service;
//    }
//
//
//    //[POST] /photos/upload
//    // 사진 업로드
//    @PostMapping("/upload")
//    public List<PhotoResponseDto> uploadBatchPhotos(
//            @RequestPart("photoData") BatchPhotoRequestDto batchPhotoRequestDto,
//            @RequestPart("files") List<MultipartFile> files) {
//
//        List<PhotoRequestDto> photoRequests = batchPhotoRequestDto.getPhotos();
//        if (photoRequests.size() != files.size()) {
//            throw new IllegalArgumentException("사진 데이터와 파일 수가 일치하지 않습니다.");
//        }
//
//        List<PhotoResponseDto> responses = new ArrayList<>();
//        // 각각의 사진에 대해 서비스 메서드를 호출
//        for (int i = 0; i < files.size(); i++) {
//            PhotoRequestDto photoDto = photoRequests.get(i);
//            MultipartFile file = files.get(i);
//
//            // S3 또는 저장소에 업로드 후 URL 반환
//            String fileUrl = s3Service.uploadFile(file);
//            photoDto.setFilePath(fileUrl);
//
//            PhotoResponseDto response = photoService.uploadPhoto(photoDto, file);
//            responses.add(response);
//        }
//        return responses;
//    }
//
//
//    // [GET] /photos/{albumId}
//    // 앨범의 사진을 조회=
//    @GetMapping("/{albumId}")
//    public ResponseEntity<List<PhotoResponseDto>> getPhotosByAlbum(@PathVariable int albumId) {
//        List<PhotoResponseDto> photos = photoService.getPhotosByAlbumId(albumId);
//        return ResponseEntity.ok(photos);
//    }
//}
