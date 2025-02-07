package com.travel.together.TravelTogether.firebase.entity;

import com.travel.together.TravelTogether.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FCMToken {

    @Id
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fcmToken;

    @Column(nullable = false)
    private LocalDateTime lastAccessTime;

    public FCMToken(String deviceId, User user, String fcmToken) {
        this.deviceId = deviceId;
        this.user = user;
        this.fcmToken = fcmToken;
    }

    @PrePersist
    protected void onCreate() {
        lastAccessTime = LocalDateTime.now();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.lastAccessTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FCMToken fcmToken = (FCMToken) o;
        return Objects.equals(deviceId, fcmToken.getDeviceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }
}