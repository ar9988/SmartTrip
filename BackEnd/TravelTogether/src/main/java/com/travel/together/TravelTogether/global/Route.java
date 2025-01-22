package com.travel.together.TravelTogether.global;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "route")
@Getter
@Setter
@NoArgsConstructor
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private Integer day;
    private Integer order;

    @Column(name = "drive_duration")
    private Integer driveDuration;

    @Column(name = "trans_duration")
    private Integer transDuration;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
}