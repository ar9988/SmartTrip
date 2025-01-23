package com.travel.together.TravelTogether.trip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "schedule")
public class Schedule {
    public Schedule() {
    }
    public Schedule(Integer id, String placeName, Trip trip, Integer orderNum, Integer day, Double lat, Double lng, Integer type, List<Route> routes) {
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
    private Integer id;

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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}