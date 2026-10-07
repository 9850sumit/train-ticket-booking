package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.TrainRouteRequest;
import com.trainbooking.dto.TrainRouteResponse;
import com.trainbooking.service.TrainRouteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TrainRouteController {

    private final TrainRouteService trainRouteService;

    public TrainRouteController(
            TrainRouteService trainRouteService) {
        this.trainRouteService = trainRouteService;
    }

    @PostMapping("/trains/{trainId}/routes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainRouteResponse> createRoute(
            @PathVariable Long trainId,
            @Valid @RequestBody TrainRouteRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trainRouteService.createRoute(
                        trainId,
                        request,
                        authentication.getName()));
    }

    @GetMapping("/trains/{trainId}/routes")
    public ResponseEntity<List<TrainRouteResponse>> getRoutes(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                trainRouteService.getRoutesByTrain(trainId));
    }

    @GetMapping("/routes/{id}")
    public ResponseEntity<TrainRouteResponse> getRoute(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                trainRouteService.getRouteById(id));
    }

    @PutMapping("/routes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainRouteResponse> updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody TrainRouteRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                trainRouteService.updateRoute(
                        id,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/routes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateRoute(
            @PathVariable Long id,
            Authentication authentication) {

        trainRouteService.deactivateRoute(
                id,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}