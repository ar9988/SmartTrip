package com.travel.together.TravelTogether.aiPlanning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "traveling_spot")
public class TravelingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "region", nullable = false, length = 20)
    private String region;

    @Column(name = "region_number", nullable = false)
    private String regionNumber;

    @Column(name = "content_id")
    private String contentId;

    @Column(name = "title")
    private String title;

    @Column(name = "overview")
    private String overView;

    @Column(name = "address")
    private String address;

    @Column(name = "image_url")
    private String imageUrl;
}
