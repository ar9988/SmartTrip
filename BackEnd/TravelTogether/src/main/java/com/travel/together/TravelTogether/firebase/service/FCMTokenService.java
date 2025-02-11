package com.travel.together.TravelTogether.firebase.service;

import com.google.firebase.messaging.*;
import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.firebase.entity.FCMToken;
import com.travel.together.TravelTogether.firebase.repository.FCMTokenRepository;
import com.travel.together.TravelTogether.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public FCMTokenService(FCMTokenRepository fcmTokenRepository, UserRepository userRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.userRepository = userRepository;
    }

    public void saveFCMToken(Long userId, String deviceId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // deviceId로 기존 토큰이 있는지 확인
        fcmTokenRepository.findById(deviceId).ifPresentOrElse(
                // 있으면 update
                existingToken -> {
                    existingToken.setUser(user);
                    existingToken.updateToken(fcmToken);
                    fcmTokenRepository.save(existingToken);
                },
                // 없으면 새로 생성
                () -> {
                    FCMToken newToken = new FCMToken(deviceId, user, fcmToken);
                    fcmTokenRepository.save(newToken);
                }
        );
//        sendNotificationToUser(userId, "fcm 토큰 확인 deviceId"+deviceId,"fcm등록 완료");
    }

    public void updateFCMToken(Long userId, String deviceId, String newToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FCMToken fcmToken = fcmTokenRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));


        fcmToken.updateToken(newToken);
    }

    public void deleteFCMToken(Long userId, String deviceId) {
        FCMToken fcmToken = fcmTokenRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        

        fcmTokenRepository.delete(fcmToken);
    }

    public void sendNotificationToUser(Long userId, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<FCMToken> userTokens = fcmTokenRepository.findByUser(user);

        if (userTokens.isEmpty()) {
            return;
        }

        for (FCMToken tokenEntity : userTokens) {
            try {
                Message message = Message.builder()
                        .setToken(tokenEntity.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {

                // 토큰이 더 이상 유효하지 않은 경우
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                        e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                    fcmTokenRepository.delete(tokenEntity);
                }
            }
        }
    }
}