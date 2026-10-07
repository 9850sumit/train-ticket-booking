package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.trainbooking.dto.TrainRequest;
import com.trainbooking.dto.TrainResponse;
import com.trainbooking.service.TrainService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainService trainService;

    public TrainController(
            TrainService trainService) {

        this.trainService = trainService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainResponse> createTrain(
            @Valid @RequestBody TrainRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        trainService.createTrain(
                                request,
                                authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TrainResponse>> getAllActiveTrains() {

        return ResponseEntity.ok(
                trainService.getAllActiveTrains());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrainResponse>> getMyTrains(
            Authentication authentication) {

        return ResponseEntity.ok(
                trainService.getMyTrains(
                        authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainResponse> getTrainById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                trainService.getTrainById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainResponse> updateTrain(
            @PathVariable Long id,
            @Valid @RequestBody TrainRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                trainService.updateTrain(
                        id,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateTrain(
            @PathVariable Long id,
            Authentication authentication) {

        trainService.deactivateTrain(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}