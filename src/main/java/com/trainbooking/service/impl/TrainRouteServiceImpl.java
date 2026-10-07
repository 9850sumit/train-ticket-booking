package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.TrainRouteRequest;
import com.trainbooking.dto.TrainRouteResponse;
import com.trainbooking.entity.Role;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.TrainRoute;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.TrainRouteRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.TrainRouteService;

@Service
@Transactional
public class TrainRouteServiceImpl implements TrainRouteService {

    private final TrainRouteRepository trainRouteRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    public TrainRouteServiceImpl(
            TrainRouteRepository trainRouteRepository,
            TrainRepository trainRepository,
            UserRepository userRepository) {

        this.trainRouteRepository = trainRouteRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TrainRouteResponse createRoute(
            Long trainId,
            TrainRouteRequest request,
            String adminEmail) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (trainRouteRepository.existsByTrainAndRouteName(
                train,
                request.getRouteName())) {

            throw new DuplicateResourceException(
                    "Route name already exists for this train");
        }

        TrainRoute route = new TrainRoute();

        route.setTrain(train);
        route.setRouteName(request.getRouteName());
        route.setActive(true);

        return mapToResponse(
                trainRouteRepository.save(route));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainRouteResponse> getRoutesByTrain(
            Long trainId) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));

        return trainRouteRepository
                .findByTrainAndActiveTrue(train)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainRouteResponse getRouteById(
            Long id) {

        TrainRoute route = trainRouteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + id));

        return mapToResponse(route);
    }

    @Override
    public TrainRouteResponse updateRoute(
            Long id,
            TrainRouteRequest request,
            String adminEmail) {

        TrainRoute route = trainRouteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + id));

        Train train = route.getTrain();

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (!Boolean.TRUE.equals(route.getActive())) {
            throw new IllegalStateException(
                    "Route is inactive");
        }

        if (!route.getRouteName().equals(
                request.getRouteName())
                && trainRouteRepository
                        .existsByTrainAndRouteName(
                                train,
                                request.getRouteName())) {

            throw new DuplicateResourceException(
                    "Route name already exists for this train");
        }

        route.setRouteName(
                request.getRouteName());

        return mapToResponse(
                trainRouteRepository.save(route));
    }

    @Override
    public void deactivateRoute(
            Long id,
            String adminEmail) {

        TrainRoute route = trainRouteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Route not found with id: " + id));

        Train train = route.getTrain();

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (!Boolean.TRUE.equals(route.getActive())) {
            throw new IllegalStateException(
                    "Route is already inactive");
        }

        route.setActive(false);

        trainRouteRepository.save(route);
    }

    private User getAdmin(String adminEmail) {

        User admin = userRepository
                .findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin user not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only administrators can manage routes");
        }

        if (!Boolean.TRUE.equals(admin.getActive())) {
            throw new AccessDeniedException(
                    "Admin account is inactive");
        }

        return admin;
    }

    private void validateTrainOwnership(
            Train train,
            User admin) {

        if (train == null
                || train.getCreatedBy() == null
                || !train.getCreatedBy()
                        .getId()
                        .equals(admin.getId())) {

            throw new AccessDeniedException(
                    "You can only manage routes for trains created by you");
        }
    }

    private TrainRouteResponse mapToResponse(
            TrainRoute route) {

        TrainRouteResponse response =
                new TrainRouteResponse();

        response.setId(
                route.getId());

        response.setTrainId(
                route.getTrain().getId());

        response.setRouteName(
                route.getRouteName());

        response.setActive(
                route.getActive());

        response.setCreatedAt(
                route.getCreatedAt());

        response.setUpdatedAt(
                route.getUpdatedAt());

        return response;
    }
}