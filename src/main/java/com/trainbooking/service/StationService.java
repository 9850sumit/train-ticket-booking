package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.StationRequest;
import com.trainbooking.dto.StationResponse;

public interface StationService {

    StationResponse createStation(StationRequest request);

    List<StationResponse> getAllStations();

    StationResponse getStationById(Long id);

    StationResponse updateStation(Long id, StationRequest request);

    void deactivateStation(Long id);
}