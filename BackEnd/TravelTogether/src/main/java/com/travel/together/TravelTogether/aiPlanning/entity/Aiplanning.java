package com.travel.together.TravelTogether.aiPlanning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "aiplanning")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aiplanning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false) // ✅ null 방지
    private String placeName;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(nullable = false)
    private Integer type;

    @Column(nullable = false)
    private Integer positionPath;

    @Column(nullable = false)
    private Integer duration;
}

