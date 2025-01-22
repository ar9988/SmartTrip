package com.travel.together.TravelTogether.album.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class PhotoPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private PhotoAlbum album;

    @Column(length = 10, nullable = false)
    private String name;

    // 기본 생성자
    public PhotoPlace() {}

    // 생성자
    public PhotoPlace(PhotoAlbum album, String name) {
        this.album = album;
        this.name = name;
    }

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PhotoAlbum getAlbum() {
        return album;
    }

    public void setAlbum(PhotoAlbum album) {
        this.album = album;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
