package com.travel.together.TravelTogether.album.entity;

import com.travel.together.TravelTogether.auth.entity.User;
import com.travel.together.TravelTogether.comment.entity.Comment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private PhotoAlbum album;

    @ManyToOne
    @JoinColumn(name = "photo_place_id", nullable = false)
    private PhotoPlace photoPlace;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 200, nullable = false)
    private String filePath;

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;


    private float latitude;
    private float longitude;

    private LocalDateTime takenAt;
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        this.uploadedAt = LocalDateTime.now();
    }
}