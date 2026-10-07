package com.trainbooking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TrainSearchResponse {

    private Long scheduleId;

    private Long trainId;

    private String trainNumber;

    private String trainName;

    private String trainType;

    private String fromStation;

    private String toStation;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private Integer availableSeats;
}