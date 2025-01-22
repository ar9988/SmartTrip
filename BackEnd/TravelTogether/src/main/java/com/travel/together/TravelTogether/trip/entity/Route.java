package com.travel.together.TravelTogether.trip.entity;

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

    public Route(Integer id, Trip trip, Integer day, Integer order_num, Integer driveDuration, Integer transDuration, Schedule schedule) {
        this.id = id;
        this.trip = trip;
        this.day = day;
        this.order_num = order_num;
        this.driveDuration = driveDuration;
        this.transDuration = transDuration;
        this.schedule = schedule;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    private Integer day;
    private Integer order_num;

    @Column(name = "drive_duration")
    private Integer driveDuration;

    @Column(name = "trans_duration")
    private Integer transDuration;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
}