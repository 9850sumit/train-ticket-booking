package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.TrainRouteRequest;
import com.trainbooking.dto.TrainRouteResponse;

public interface TrainRouteService {

    TrainRouteResponse createRoute(
            Long trainId,
            TrainRouteRequest request,
            String adminEmail);

    List<TrainRouteResponse> getRoutesByTrain(Long trainId);

    TrainRouteResponse getRouteById(Long id);

    TrainRouteResponse updateRoute(
            Long id,
            TrainRouteRequest request,
            String adminEmail);

    void deactivateRoute(
            Long id,
            String adminEmail);
}