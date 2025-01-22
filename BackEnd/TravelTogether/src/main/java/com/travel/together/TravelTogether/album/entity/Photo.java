package com.travel.together.TravelTogether.album.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private PhotoAlbum album;

    @ManyToOne
    @JoinColumn(name = "photo_place")
    private PhotoPlace photoPlace;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 200)
    private String filePath;

    private float latitude;

    private float longitude;

    private LocalDateTime takenAt;

    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}