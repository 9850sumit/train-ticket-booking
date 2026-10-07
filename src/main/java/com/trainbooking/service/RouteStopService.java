package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.RouteStopRequest;
import com.trainbooking.dto.RouteStopResponse;

public interface RouteStopService {

    RouteStopResponse addStop(
            Long routeId,
            RouteStopRequest request,
            String adminEmail);

    List<RouteStopResponse> getStops(
            Long routeId);

    RouteStopResponse updateStop(
            Long stopId,
            RouteStopRequest request,
            String adminEmail);
}