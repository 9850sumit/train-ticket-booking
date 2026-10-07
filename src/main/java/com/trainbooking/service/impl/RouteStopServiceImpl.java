package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.RouteStopRequest;
import com.trainbooking.dto.RouteStopResponse;
import com.trainbooking.entity.Role;
import com.trainbooking.entity.RouteStop;
import com.trainbooking.entity.Station;
import com.trainbooking.entity.TrainRoute;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.RouteStopRepository;
import com.trainbooking.repository.StationRepository;
import com.trainbooking.repository.TrainRouteRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.RouteStopService;

@Service
@Transactional
public class RouteStopServiceImpl implements RouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final TrainRouteRepository trainRouteRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    public RouteStopServiceImpl(
            RouteStopRepository routeStopRepository,
            TrainRouteRepository trainRouteRepository,
            StationRepository stationRepository,
            UserRepository userRepository) {

        this.routeStopRepository = routeStopRepository;
        this.trainRouteRepository = trainRouteRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RouteStopResponse addStop(
            Long routeId,
            RouteStopRequest request,
            String adminEmail) {

        TrainRoute route =
                trainRouteRepository.findById(routeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Route not found with id: " + routeId));

        User admin = getAdmin(adminEmail);

        validateRouteOwnership(route, admin);
        validateRouteIsActive(route);

        validateRequest(request);

        Station station =
                stationRepository.findById(
                        request.getStationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Station not found with id: "
                                                + request.getStationId()));

        validateStationIsActive(station);

        if (routeStopRepository
                .existsByTrainRouteAndStopSequence(
                        route,
                        request.getStopSequence())) {

            throw new DuplicateResourceException(
                    "Stop sequence already exists for this route");
        }

        RouteStop stop = new RouteStop();

        stop.setTrainRoute(route);
        stop.setStation(station);
        stop.setStopSequence(
                request.getStopSequence());
        stop.setArrivalTime(
                request.getArrivalTime());
        stop.setDepartureTime(
                request.getDepartureTime());
        stop.setDayNumber(
                request.getDayNumber());
        stop.setDistanceFromSource(
                request.getDistanceFromSource());

        return mapToResponse(
                routeStopRepository.save(stop));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteStopResponse> getStops(
            Long routeId) {

        TrainRoute route =
                trainRouteRepository.findById(routeId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Route not found with id: " + routeId));

        return routeStopRepository
                .findByTrainRouteOrderByStopSequence(route)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public RouteStopResponse updateStop(
            Long stopId,
            RouteStopRequest request,
            String adminEmail) {

        RouteStop stop =
                routeStopRepository.findById(stopId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Route stop not found with id: "
                                                + stopId));

        TrainRoute route =
                stop.getTrainRoute();

        User admin = getAdmin(adminEmail);

        validateRouteOwnership(route, admin);
        validateRouteIsActive(route);

        validateRequest(request);

        Station station =
                stationRepository.findById(
                        request.getStationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Station not found with id: "
                                                + request.getStationId()));

        validateStationIsActive(station);

        if (routeStopRepository
                .existsByTrainRouteAndStopSequenceAndIdNot(
                        route,
                        request.getStopSequence(),
                        stopId)) {

            throw new DuplicateResourceException(
                    "Stop sequence already exists for this route");
        }

        stop.setStation(station);
        stop.setStopSequence(
                request.getStopSequence());
        stop.setArrivalTime(
                request.getArrivalTime());
        stop.setDepartureTime(
                request.getDepartureTime());
        stop.setDayNumber(
                request.getDayNumber());
        stop.setDistanceFromSource(
                request.getDistanceFromSource());

        return mapToResponse(
                routeStopRepository.save(stop));
    }

    private User getAdmin(String adminEmail) {

        User admin =
                userRepository.findByEmail(adminEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Admin user not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only administrators can manage route stops");
        }

        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new AccessDeniedException(
                    "Admin account is inactive");
        }

        return admin;
    }

    private void validateRouteOwnership(
            TrainRoute route,
            User admin) {

        if (route == null
                || route.getTrain() == null
                || route.getTrain().getCreatedBy() == null
                || !route.getTrain()
                        .getCreatedBy()
                        .getId()
                        .equals(admin.getId())) {

            throw new AccessDeniedException(
                    "You can only manage route stops for routes belonging to your trains");
        }
    }

    private void validateRouteIsActive(
            TrainRoute route) {

        if (!Boolean.TRUE.equals(route.getActive())) {
            throw new IllegalStateException(
                    "Route is inactive");
        }

        if (!Boolean.TRUE.equals(
                route.getTrain().getActive())) {

            throw new IllegalStateException(
                    "Train is inactive");
        }
    }

    private void validateStationIsActive(
            Station station) {

        if (!Boolean.TRUE.equals(
                station.getActive())) {

            throw new ResourceNotFoundException(
                    "Station is inactive");
        }
    }

    private void validateRequest(
            RouteStopRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Route stop details are required");
        }

        if (request.getStationId() == null) {
            throw new IllegalArgumentException(
                    "Station is required");
        }

        if (request.getStopSequence() == null
                || request.getStopSequence() < 1) {

            throw new IllegalArgumentException(
                    "Stop sequence must be greater than zero");
        }

        if (request.getDayNumber() == null
                || request.getDayNumber() < 1) {

            throw new IllegalArgumentException(
                    "Day number must be greater than zero");
        }

        if (request.getDistanceFromSource() != null
                && request.getDistanceFromSource().signum() < 0) {

            throw new IllegalArgumentException(
                    "Distance from source cannot be negative");
        }
        
        if (request.getArrivalTime() != null
                && request.getDepartureTime() != null
                && request.getDepartureTime()
                        .isBefore(
                                request.getArrivalTime())) {

            throw new IllegalArgumentException(
                    "Departure time cannot be before arrival time");
        }
    }

    private RouteStopResponse mapToResponse(
            RouteStop stop) {

        RouteStopResponse response =
                new RouteStopResponse();

        response.setId(stop.getId());

        response.setRouteId(
                stop.getTrainRoute().getId());

        response.setStationId(
                stop.getStation().getId());

        response.setStationCode(
                stop.getStation().getStationCode());

        response.setStationName(
                stop.getStation().getStationName());

        response.setStopSequence(
                stop.getStopSequence());

        response.setArrivalTime(
                stop.getArrivalTime());

        response.setDepartureTime(
                stop.getDepartureTime());

        response.setDayNumber(
                stop.getDayNumber());

        response.setDistanceFromSource(
                stop.getDistanceFromSource());

        return response;
    }
}