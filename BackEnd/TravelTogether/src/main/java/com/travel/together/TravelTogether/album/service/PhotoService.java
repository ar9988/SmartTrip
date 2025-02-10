package com.travel.together.TravelTogether.album.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.travel.together.TravelTogether.album.dto.PhotoRequestDto;
import com.travel.together.TravelTogether.album.dto.PhotoResponseDto;
import com.travel.together.TravelTogether.album.entity.Photo;
import com.travel.together.TravelTogether.album.entity.PhotoAlbum;
import com.travel.together.TravelTogether.album.entity.PhotoPlace;
import com.travel.together.TravelTogether.album.repository.PhotoAlbumRepository;
import com.travel.together.TravelTogether.album.repository.PhotoRepository;
import com.travel.together.TravelTogether.album.repository.PhotoPlaceRepository;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PhotoAlbumRepository photoAlbumRepository;
    private final PhotoPlaceRepository photoPlaceRepository; // 📌 PhotoPlaceRepository 추가
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // S3 업로드를 위한 의존성 주입
    public PhotoService(PhotoRepository photoRepository,
                        PhotoAlbumRepository photoAlbumRepository,
                        PhotoPlaceRepository photoPlaceRepository,
                        UserRepository userRepository,
                        S3Service s3Service) {
        this.photoRepository = photoRepository;
        this.photoAlbumRepository = photoAlbumRepository;
        this.photoPlaceRepository = photoPlaceRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }


    // 사진 업로드 및 S3 저장 후 DB 저장
    @Transactional
    public PhotoResponseDto uploadPhoto(PhotoRequestDto photoRequestDto, MultipartFile file) {
        // 파일 전체 바이트 배열 읽기 (재사용을 위해)
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("파일 바이트 읽기 실패", e);
        }

        // EXIF 정보 파싱
        Float latitude = null;
        Float longitude = null;
        LocalDateTime takenAt = null;
        try (ByteArrayInputStream exifInputStream = new ByteArrayInputStream(fileBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(exifInputStream);

            // GPS 정보
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if (geoLocation != null && !Double.isNaN(geoLocation.getLatitude()) && !Double.isNaN(geoLocation.getLongitude())) {
                    latitude = (float) geoLocation.getLatitude();
                    longitude = (float) geoLocation.getLongitude();
                }
            }

            // 촬영 일시
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null) {
                Date originalDate = exifDirectory.getDateOriginal();
                if (originalDate != null) {
                    takenAt = originalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            }
        } catch (Exception e) {
            System.err.println("EXIF 파싱 실패: " + e.getMessage());
        }

        // S3 업로드 후 URL 저장
        String s3Url;
        try (ByteArrayInputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
            s3Url = s3Service.uploadFile(uploadStream, file.getSize(), file.getContentType(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 에러 발생", e);
        }

        // EXIF 데이터를 사용할 수 없으면 DTO 값 사용
        photoRequestDto.setLatitude(latitude != null ? latitude : photoRequestDto.getLatitude());
        photoRequestDto.setLongitude(longitude != null ? longitude : photoRequestDto.getLongitude());
        photoRequestDto.setTakenAt(takenAt != null ? takenAt.toString() : photoRequestDto.getTakenAt());
        photoRequestDto.setFilePath(s3Url); // ✅ S3 URL을 filePath로 설정

        // 새로 만든 `savePhoto` 메서드를 사용해 DB 저장
        return savePhoto(photoRequestDto);
    }

    // 이미 S3에 저장된 사진을 DB에 저장하는 메서드
    @Transactional
    public PhotoResponseDto savePhoto(PhotoRequestDto photoRequestDto) {
        // 앨범 정보 조회
        PhotoAlbum album = photoAlbumRepository.findById(photoRequestDto.getAlbumId())
                .orElseThrow(() -> new RuntimeException("PhotoAlbum not found with id: " + photoRequestDto.getAlbumId()));

        // 유저 정보 조회
        User user = userRepository.findById(photoRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + photoRequestDto.getUserId()));

        // place(장소) 데이터를 기반으로 PhotoPlace 조회 또는 생성
        PhotoPlace photoPlace = photoPlaceRepository.findByNameAndAlbum(photoRequestDto.getPlace(), album)
                .orElseGet(() -> {
                    PhotoPlace newPlace = new PhotoPlace();
                    newPlace.setAlbum(album);
                    newPlace.setName(photoRequestDto.getPlace()); // 클라이언트가 보낸 place 값 저장
                    return photoPlaceRepository.save(newPlace);
                });


        // Photo 엔티티 생성 및 필드 설정
        Photo photo = new Photo();
        photo.setAlbum(album);
        photo.setPhotoPlace(photoPlace);
        photo.setUser(user);
        photo.setFilePath(photoRequestDto.getFilePath());
        photo.setLatitude(photoRequestDto.getLatitude());
        photo.setLongitude(photoRequestDto.getLongitude());

        // 촬영 날짜 설정
        if (photoRequestDto.getTakenAt() != null && !photoRequestDto.getTakenAt().isEmpty()) {
            photo.setTakenAt(LocalDateTime.parse(photoRequestDto.getTakenAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        // DB 저장
        Photo savedPhoto = photoRepository.save(photo);

        // Response DTO 변환
        PhotoResponseDto responseDto = new PhotoResponseDto();
        responseDto.setAlbumId(savedPhoto.getAlbum().getId());
        responseDto.setPhotoId(savedPhoto.getId());
        responseDto.setUserId(savedPhoto.getUser().getId());
        responseDto.setLatitude(savedPhoto.getLatitude());
        responseDto.setLongitude(savedPhoto.getLongitude());
        responseDto.setFilePath(savedPhoto.getFilePath());
        responseDto.setTakenAt(savedPhoto.getTakenAt() != null ? savedPhoto.getTakenAt().toString() : null);
        responseDto.setPlace(savedPhoto.getPhotoPlace().getName());

        return responseDto;
    }

   // 앨범 사진 조회
    @Transactional(readOnly = true)
    public List<PhotoResponseDto> getPhotosByAlbumId(int albumId) {
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        return photos.stream().map(photo -> {
            PhotoResponseDto dto = new PhotoResponseDto();
            dto.setAlbumId(photo.getAlbum().getId());
            dto.setPhotoId(photo.getId());
            dto.setUserId(photo.getUser().getId());
            dto.setLatitude(photo.getLatitude());
            dto.setLongitude(photo.getLongitude());
            dto.setFilePath(photo.getFilePath());
            dto.setTakenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null);
            dto.setPlace(photo.getPhotoPlace().getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean deletePhoto(int albumId, Integer photoId, Long userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));

        if (!photo.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this photo");
        }

        int albumIdFromPhoto = (photo.getAlbum() != null) ? photo.getAlbum().getId() : -1;
        if (albumIdFromPhoto != albumId) {
//            throw new IllegalArgumentException("Photo does not belong to the album");
            return false;
        }

        // 1. S3에서 사진 삭제
        String fileKey = extractFileKeyFromUrl(photo.getFilePath()); // URL에서 파일 키 추출
        s3Service.deleteFile(fileKey);

        // 2. 데이터베이스에서 삭제
        photoRepository.delete(photo);

        return true;
    }

    private String extractFileKeyFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1); // S3 URL에서 키 추출
    }
}
