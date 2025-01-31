package com.travel.together.TravelTogether.aiPlanning.controller;


import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsRequestDto;
import com.travel.together.TravelTogether.aiPlanning.dto.DirectionsResponseDto;
import com.travel.together.TravelTogether.aiPlanning.service.DirectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/directions")
public class DirectionsController {

    private final DirectionsService directionsService;

    @Autowired
    public DirectionsController(DirectionsService directionsService) {
        this.directionsService = directionsService;
    }

    @PostMapping("/driving")
    public DirectionsResponseDto getDrivingDirections(@RequestBody DirectionsRequestDto directionsRequestDto) {
        return directionsService.getDrivingDirections(directionsRequestDto);
    }
}
