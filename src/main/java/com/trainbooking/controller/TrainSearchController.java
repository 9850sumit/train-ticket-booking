package com.trainbooking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.TrainSearchResponse;
import com.trainbooking.service.TrainSearchService;

@RestController
@RequestMapping("/api/trains")
public class TrainSearchController {

    private final TrainSearchService trainSearchService;

    public TrainSearchController(TrainSearchService trainSearchService) {
        this.trainSearchService = trainSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrainSearchResponse>> searchTrains(
            @RequestParam Long fromStationId,
            @RequestParam Long toStationId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate journeyDate) {

        return ResponseEntity.ok(
                trainSearchService.searchTrains(
                        fromStationId,
                        toStationId,
                        journeyDate
                )
        );
    }
}