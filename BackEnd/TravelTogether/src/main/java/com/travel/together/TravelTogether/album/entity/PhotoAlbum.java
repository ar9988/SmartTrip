package com.travel.together.TravelTogether.album.entity;

import com.travel.together.TravelTogether.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PhotoAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @Column(length = 200, nullable = false)
    private String imageUrl;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoPlace> photoPlaces;
}