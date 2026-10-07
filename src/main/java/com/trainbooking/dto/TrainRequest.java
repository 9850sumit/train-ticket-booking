package com.trainbooking.dto;

import com.trainbooking.entity.TrainType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TrainRequest {

    @NotBlank(message = "Train number is required")
    @Size(max = 20, message = "Train number cannot exceed 20 characters")
    private String trainNumber;

    @NotBlank(message = "Train name is required")
    @Size(max = 150, message = "Train name cannot exceed 150 characters")
    private String trainName;

    @NotNull(message = "Train type is required")
    private TrainType trainType;

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }
}