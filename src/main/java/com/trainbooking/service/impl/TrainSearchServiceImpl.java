package com.trainbooking.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.TrainSearchResponse;
import com.trainbooking.entity.RouteStop;
import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.Station;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.TrainRoute;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.RouteStopRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.TrainRouteRepository;
import com.trainbooking.service.ScheduleResolver;
import com.trainbooking.service.TrainSearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainSearchServiceImpl implements TrainSearchService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final TrainRouteRepository trainRouteRepository;
    private final RouteStopRepository routeStopRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ScheduleResolver scheduleResolver;

    @Override
    @Transactional
    public List<TrainSearchResponse> searchTrains(
            Long fromStationId,
            Long toStationId,
            LocalDate journeyDate) {

        if (fromStationId == null
                || toStationId == null
                || journeyDate == null) {

            throw new IllegalArgumentException(
                    "Source station, destination station and journey date are required"
            );
        }

        if (fromStationId.equals(toStationId)) {

            throw new IllegalArgumentException(
                    "Source and destination stations cannot be the same"
            );
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        if (journeyDate.isBefore(today)) {

            throw new IllegalArgumentException(
                    "Journey date cannot be in the past"
            );
        }

        Station fromStation =
                stationRepository.findById(fromStationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Source station not found"
                                ));

        Station toStation =
                stationRepository.findById(toStationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination station not found"
                                ));

        if (!Boolean.TRUE.equals(fromStation.getActive())) {

            throw new IllegalStateException(
                    "Source station is inactive"
            );
        }

        if (!Boolean.TRUE.equals(toStation.getActive())) {

            throw new IllegalStateException(
                    "Destination station is inactive"
            );
        }

        List<Train> trains =
                trainRepository.findByActiveTrue();

        List<TrainSearchResponse> results =
                new ArrayList<>();

        for (Train train : trains) {

            if (train == null
                    || !Boolean.TRUE.equals(train.getActive())) {
                continue;
            }

            List<TrainRoute> routes =
                    trainRouteRepository
                            .findByTrainAndActiveTrue(train);

            for (TrainRoute route : routes) {

                List<RouteStop> stops =
                        routeStopRepository
                                .findByTrainRouteOrderByStopSequence(
                                        route);

                RouteStop sourceStop =
                        stops.stream()
                                .filter(stop ->
                                        stop.getStation()
                                                .getId()
                                                .equals(fromStationId))
                                .findFirst()
                                .orElse(null);

                RouteStop destinationStop =
                        stops.stream()
                                .filter(stop ->
                                        stop.getStation()
                                                .getId()
                                                .equals(toStationId))
                                .findFirst()
                                .orElse(null);

                if (sourceStop == null
                        || destinationStop == null) {
                    continue;
                }

                if (sourceStop.getStopSequence()
                        >= destinationStop.getStopSequence()) {
                    continue;
                }

                Schedule schedule =
                        scheduleResolver.resolveSchedule(
                                train,
                                journeyDate);

                if (schedule == null) {
                    break;
                }

                if (schedule.getStatus()
                        != ScheduleStatus.SCHEDULED) {
                    break;
                }

                int availableSeats =
                        (int) scheduleSeatRepository
                                .countAvailableOrExpiredHeldSeats(
                                        schedule.getId(),
                                        LocalDateTime.now(
                                                BUSINESS_ZONE)
                                );

                TrainSearchResponse response =
                        new TrainSearchResponse();

                response.setScheduleId(
                        schedule.getId());

                response.setTrainId(
                        train.getId());

                response.setTrainNumber(
                        train.getTrainNumber());

                response.setTrainName(
                        train.getTrainName());

                response.setTrainType(
                        train.getTrainType().name());

                response.setFromStation(
                        fromStation.getStationName());

                response.setToStation(
                        toStation.getStationName());

                response.setDepartureTime(
                        sourceStop.getDepartureTime());

                response.setArrivalTime(
                        destinationStop.getArrivalTime());

                response.setAvailableSeats(
                        availableSeats);

                results.add(response);

                break;
            }
        }

        results.sort(
                Comparator.comparing(
                        TrainSearchResponse::getDepartureTime,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
        );

        return results;
    }
}