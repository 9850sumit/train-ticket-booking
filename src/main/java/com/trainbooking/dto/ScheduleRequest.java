package com.trainbooking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public class ScheduleRequest {

    @NotNull(message = "Journey date is required")
    @FutureOrPresent(message = "Journey date cannot be in the past")
    private LocalDate journeyDate;

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }
}