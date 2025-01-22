package com.travel.together.TravelTogether.album.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PhotoAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(length = 200)
    private String imageUrl;
}