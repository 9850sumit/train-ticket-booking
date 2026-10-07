package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.TrainRequest;
import com.trainbooking.dto.TrainResponse;

public interface TrainService {

    TrainResponse createTrain(
            TrainRequest request,
            String adminEmail);

    List<TrainResponse> getAllActiveTrains();

    List<TrainResponse> getMyTrains(
            String adminEmail);

    TrainResponse getTrainById(
            Long id);

    TrainResponse updateTrain(
            Long id,
            TrainRequest request,
            String adminEmail);

    void deactivateTrain(
            Long id,
            String adminEmail);
}