package com.travel.together.TravelTogether.firebase.controller;

import com.travel.together.TravelTogether.firebase.service.FCMTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
public class FCMTokenController {

    private final FCMTokenService fcmTokenService;

    public FCMTokenController(FCMTokenService fcmTokenService) {
        this.fcmTokenService = fcmTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<Void> saveFCMToken(
            @RequestParam Long userId,
            @RequestParam String deviceId,
            @RequestParam String fcmToken) {

        fcmTokenService.saveFCMToken(userId, deviceId, fcmToken);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/token")
    public ResponseEntity<Void> updateFCMToken(
            @RequestParam Long userId,
            @RequestParam String deviceId,
            @RequestParam String fcmToken) {

        fcmTokenService.updateFCMToken(userId, deviceId, fcmToken);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token")
    public ResponseEntity<Void> deleteFCMToken(
            @RequestParam Long userId,
            @RequestParam String deviceId) {

        fcmTokenService.deleteFCMToken(userId, deviceId);
        return ResponseEntity.ok().build();
    }

}
