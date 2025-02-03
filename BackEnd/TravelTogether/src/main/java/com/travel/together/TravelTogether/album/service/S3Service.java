package com.travel.together.TravelTogether.album.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, @Value("${AWS_S3_BUCKET_NAME}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    // MultipartFile을 입력받아 S3에 업로드하는 기본 메서드

    public String uploadFile(MultipartFile file) {
        // 원본 파일명에 UUID를 접두어로 붙여 고유한 파일 이름 생성
        String fileName = generateFileName(file.getOriginalFilename());

        // try-with-resources를 사용하여 InputStream을 자동으로 닫습니다.
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    // 업로드된 파일에 공개 읽기 권한 부여 (필요에 따라 변경)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            S3Utilities utilities = s3Client.utilities();
            return utilities.getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 에러 발생", e);
        }
    }


    // InputStream, 파일 크기, MIME 타입, 원본 파일명을 입력받아 S3에 업로드하는 오버로딩 메서드입니다.
    // EXIF 정보를 파싱한 후, 새로 생성한 ByteArrayInputStream을 전달할 때 유용합니다.

    public String uploadFile(InputStream inputStream, long contentLength, String contentType, String originalFilename) {
        String fileName = generateFileName(originalFilename);

        // 전달받은 InputStream을 try-with-resources로 감싸서 업로드 후 자동으로 닫습니다.
        try (InputStream is = inputStream) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, contentLength));

            S3Utilities utilities = s3Client.utilities();
            return utilities.getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 에러 발생", e);
        }
    }

    // 고유한 파일 이름을 생성
    private String generateFileName(String originalFilename) {
        String sanitizedFilename = originalFilename.replace(" ", "_");
        return UUID.randomUUID().toString() + "_" + sanitizedFilename;
    }
}
