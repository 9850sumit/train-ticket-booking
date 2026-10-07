package com.trainbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CoachRequest {

    @NotBlank
    @Size(max = 20)
    private String coachNumber;

    @NotBlank
    @Size(max = 30)
    private String coachType;

    @NotNull
    @Min(1)
    private Integer seatCapacity;

    public String getCoachNumber() {
        return coachNumber;
    }

    public void setCoachNumber(String coachNumber) {
        this.coachNumber = coachNumber;
    }

    public String getCoachType() {
        return coachType;
    }

    public void setCoachType(String coachType) {
        this.coachType = coachType;
    }

    public Integer getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(Integer seatCapacity) {
        this.seatCapacity = seatCapacity;
    }
}