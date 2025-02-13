package com.travel.together.TravelTogether.aiPlanning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "travelingspot")
public class TravelingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "region", nullable = false, length = 20)
    private String region;

    @Column(name = "regionnumber", nullable = false)
    private String regionNumber;

    @Column(name = "contentid")
    private String contentid;

    @Column(name = "title")
    private String title;

    @Column(name = "overview")
    private String overview;

    @Column(name = "address")
    private String address;

    @Column(name = "imageurl")
    private String imageurl;
}
