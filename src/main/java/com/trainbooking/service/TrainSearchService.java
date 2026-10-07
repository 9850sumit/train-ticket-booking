package com.trainbooking.service;

import com.trainbooking.dto.TrainSearchResponse;

import java.time.LocalDate;
import java.util.List;

public interface TrainSearchService {

    List<TrainSearchResponse> searchTrains(
            Long fromStationId,
            Long toStationId,
            LocalDate journeyDate
    );
}