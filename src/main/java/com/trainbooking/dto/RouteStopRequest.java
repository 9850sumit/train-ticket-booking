package com.trainbooking.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RouteStopRequest {

    @NotNull
    private Long stationId;

    @NotNull
    @Positive
    private Integer stopSequence;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    @NotNull
    @Positive
    private Integer dayNumber;

    private BigDecimal distanceFromSource;

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(Integer stopSequence) {
        this.stopSequence = stopSequence;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public BigDecimal getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(BigDecimal distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }
}