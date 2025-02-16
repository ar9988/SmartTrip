package com.travel.together.TravelTogether.trip.entity;

import com.travel.together.TravelTogether.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "trip_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_owner")
    private Boolean isOwner;

    public TripMember(Trip trip, User user){
        this.trip = trip;
        this.user = user;
        this.isOwner = false;
    }



}

