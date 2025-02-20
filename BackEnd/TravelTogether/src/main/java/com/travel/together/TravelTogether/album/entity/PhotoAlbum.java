package com.travel.together.TravelTogether.album.entity;

import com.travel.together.TravelTogether.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(id, photo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}