package com.travel.together.TravelTogether.trip.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "schedule")
public class Schedule {
    public Schedule(Long id, String placeName, Trip trip, Integer orderNum, Integer day, Double lat, Double lng, Integer type, List<Route> routes) {
        this.id = id;
        this.placeName = placeName;
        this.trip = trip;
        this.orderNum = orderNum;
        this.day = day;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
        this.routes = routes;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_name", length = 20)
    private String placeName;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "order_num")
    private Integer orderNum;

    private Integer day;
    private Double lat;
    private Double lng;
    private Integer type;

    @OneToMany(mappedBy = "schedule")
    private List<Route> routes;

    // Getters and Setters

}