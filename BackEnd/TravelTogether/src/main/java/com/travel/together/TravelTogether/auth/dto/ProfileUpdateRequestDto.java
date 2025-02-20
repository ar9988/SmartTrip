package com.travel.together.TravelTogether.auth.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileUpdateRequestDto {
    private String name;
    private String profile;
    private MultipartFile profileImage;
}
