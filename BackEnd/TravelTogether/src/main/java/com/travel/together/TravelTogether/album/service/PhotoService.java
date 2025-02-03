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
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // S3 업로드를 위한 의존성 주입
    public PhotoService(PhotoRepository photoRepository,
                        PhotoAlbumRepository photoAlbumRepository,
                        UserRepository userRepository,
                        S3Service s3Service) {
        this.photoRepository = photoRepository;
        this.photoAlbumRepository = photoAlbumRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    // 사진 업로드
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
            // 파싱 실패해도 업로드는 진행
            System.err.println("EXIF 파싱 실패: " + e.getMessage());
        }

        // S3 업로드를 위해 fileBytes를 새로운 ByteArrayInputStream으로 변환하여 사용
        String s3Url;
        try (ByteArrayInputStream uploadStream = new ByteArrayInputStream(fileBytes)) {
            // 기존 S3Service.uploadFile(MultipartFile file) 대신, 오버로딩된 메서드를 사용합니다.
            // 예제에서는 S3Service에 다음과 같은 메서드가 있다고 가정합니다:
            // uploadFile(InputStream inputStream, long contentLength, String contentType, String originalFilename)
            s3Url = s3Service.uploadFile(uploadStream, file.getSize(), file.getContentType(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 에러 발생", e);
        }

        // 앨범 정보 조회
        Optional<PhotoAlbum> albumOpt = photoAlbumRepository.findById(photoRequestDto.getAlbumId());
        if (!albumOpt.isPresent()) {
            throw new RuntimeException("PhotoAlbum not found with id: " + photoRequestDto.getAlbumId());
        }
        PhotoAlbum album = albumOpt.get();

        Optional<User> userOpt = userRepository.findById(photoRequestDto.getUserId());
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found with id: " + photoRequestDto.getUserId());
        }
        User user = userOpt.get();

        // PhotoPlace : 기존 장소가 있으면 사용, 없으면 "Default"
        PhotoPlace photoPlace;
        if (album.getPhotoPlaces() != null && !album.getPhotoPlaces().isEmpty()) {
            photoPlace = album.getPhotoPlaces().get(0);
        } else {
            photoPlace = new PhotoPlace();
            photoPlace.setAlbum(album);
            photoPlace.setName("Default");
            album.getPhotoPlaces().add(photoPlace);
        }

        // Photo 엔티티 생성 및 필드 설정
        Photo photo = new Photo();
        photo.setAlbum(album);
        photo.setPhotoPlace(photoPlace);
        photo.setUser(user);
        photo.setFilePath(s3Url);

        // EXIF 값이 있으면 사용, 없으면 DTO의 값 사용
        if (latitude != null && longitude != null) {
            photo.setLatitude(latitude);
            photo.setLongitude(longitude);
        } else {
            photo.setLatitude(photoRequestDto.getLatitude());
            photo.setLongitude(photoRequestDto.getLongitude());
        }
        if (takenAt != null) {
            photo.setTakenAt(takenAt);
        } else if (photoRequestDto.getTakenAt() != null && !photoRequestDto.getTakenAt().isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            photo.setTakenAt(LocalDateTime.parse(photoRequestDto.getTakenAt(), formatter));
        }

        // DB 저장 DTO 반환
        Photo savedPhoto = photoRepository.save(photo);
        PhotoResponseDto responseDto = new PhotoResponseDto();
        responseDto.setAlbumId(savedPhoto.getAlbum().getId());
        responseDto.setUserId(savedPhoto.getUser().getId());
        responseDto.setLatitude(savedPhoto.getLatitude());
        responseDto.setLongitude(savedPhoto.getLongitude());
        responseDto.setFilePath(savedPhoto.getFilePath());
        responseDto.setTakenAt(savedPhoto.getTakenAt() != null ? savedPhoto.getTakenAt().toString() : null);
        return responseDto;
    }


    // 앨법에 속한 사진 조회
    @Transactional(readOnly = true)
    public List<PhotoResponseDto> getPhotosByAlbumId(int albumId) {
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        return photos.stream().map(photo -> {
            PhotoResponseDto dto = new PhotoResponseDto();
            dto.setAlbumId(photo.getAlbum().getId());
            dto.setUserId(photo.getUser().getId());
            dto.setLatitude(photo.getLatitude());
            dto.setLongitude(photo.getLongitude());
            dto.setFilePath(photo.getFilePath());
            dto.setTakenAt(photo.getTakenAt() != null ? photo.getTakenAt().toString() : null);
            return dto;
        }).collect(Collectors.toList());
    }
}
