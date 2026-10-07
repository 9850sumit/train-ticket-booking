package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.trainbooking.dto.RouteStopRequest;
import com.trainbooking.dto.RouteStopResponse;
import com.trainbooking.service.RouteStopService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/routes")
public class RouteStopController {

    private final RouteStopService routeStopService;

    public RouteStopController(
            RouteStopService routeStopService) {

        this.routeStopService = routeStopService;
    }

    @PostMapping("/{routeId}/stops")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteStopResponse> addStop(
            @PathVariable Long routeId,
            @Valid @RequestBody RouteStopRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        routeStopService.addStop(
                                routeId,
                                request,
                                authentication.getName()));
    }

    @GetMapping("/{routeId}/stops")
    public ResponseEntity<List<RouteStopResponse>> getStops(
            @PathVariable Long routeId) {

        return ResponseEntity.ok(
                routeStopService.getStops(routeId));
    }

    @PutMapping("/stops/{stopId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteStopResponse> updateStop(
            @PathVariable Long stopId,
            @Valid @RequestBody RouteStopRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                routeStopService.updateStop(
                        stopId,
                        request,
                        authentication.getName()));
    }
}