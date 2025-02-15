package com.travel.together.TravelTogether.trip.service;

import com.travel.together.TravelTogether.auth.repository.UserRepository;
import com.travel.together.TravelTogether.trip.entity.Day;
import com.travel.together.TravelTogether.trip.entity.Schedule;
import com.travel.together.TravelTogether.trip.entity.Trip;
import com.travel.together.TravelTogether.trip.entity.TripMember;
import com.travel.together.TravelTogether.trip.repository.DayRepository;
import com.travel.together.TravelTogether.trip.repository.TripMemberRepository;
import com.travel.together.TravelTogether.trip.repository.TripRepository;
import com.travel.together.TravelTogether.tripwebsocket.dto.DayDto;
import com.travel.together.TravelTogether.tripwebsocket.dto.MemberDTO;
import com.travel.together.TravelTogether.tripwebsocket.dto.ScheduleDTO;
import com.travel.together.TravelTogether.tripwebsocket.dto.TripDetailDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripService {
    public TripService(DayRepository dayRepository, TripMemberRepository tripMemberRepository, TripRepository tripRepository, UserRepository userRepository) {
        this.dayRepository = dayRepository;
        this.tripMemberRepository = tripMemberRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    private final DayRepository dayRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public List<Day> getDaysByTripId(Integer tripId) {
        // Trip의 day 정보를 조회하는 로직
        return dayRepository.findByTripId(tripId);
    }

    public List<TripMember> getMembersByTripId(Integer tripId) {
        // Trip의 member 정보를 조회하는 로직
        return tripMemberRepository.findByTripId(tripId);
    }

    public TripDetailDTO getTripDetailById(Integer tripId) {
        log.info("Getting trip details for tripId: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found"));
        log.info("Found trip: {}", trip.getId());


        List<MemberDTO> members = tripMemberRepository.findByTripId(tripId)
                .stream()
                .map(member -> {
                    log.info("Processing member: {}", member.getId());
                    return new MemberDTO(
                            member.getUser().getId().toString(),
                            member.getUser().getName(),
                            member.getIsOwner(),
                            member.getUser().getProfile_image());
                })
                .collect(Collectors.toList());

        log.info("Found {} members", Optional.of(members.size()));


        List<Day> days = dayRepository.findByTripId(tripId);

        log.info("Found {} days", Optional.of(days.size()));


        List<DayDto> dayDtos = days.stream()
                .map(day -> {
                    try {
                        return new DayDto(
                                day.getStartTime(),
                                day.getSchedules().stream()
                                        .sorted(Comparator.comparing(Schedule::getOrderNum))
                                        .map(schedule -> new ScheduleDTO(
                                                schedule.getId(),
                                                schedule.getPlaceName(),
                                                System.currentTimeMillis(), // 현재 시간을 timestamp로
                                                schedule.getPositionPath(), // positionPath 추가
                                                schedule.getDuration(),
                                                schedule.getLat(),
                                                schedule.getLng(),
                                                schedule.getType()))
                                        .collect(Collectors.toList()));
                    } catch (Exception e) {
                        log.error("Error processing day {}: {}", day.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("Found {} days", Optional.of(days.size()));


        return new TripDetailDTO(
                trip.getId(),
                trip.getTitle(),
                members,
                dayDtos,
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }




}