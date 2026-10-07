package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.StationRequest;
import com.trainbooking.dto.StationResponse;
import com.trainbooking.service.StationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StationResponse> createStation(
            @Valid @RequestBody StationRequest request) {

        StationResponse response = stationService.createStation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {

        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(stationService.getStationById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StationResponse> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody StationRequest request) {

        return ResponseEntity.ok(
                stationService.updateStation(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateStation(
            @PathVariable Long id) {

        stationService.deactivateStation(id);

        return ResponseEntity.noContent().build();
    }
}