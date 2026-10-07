package com.trainbooking.dto;

import com.trainbooking.entity.SeatType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SeatRequest {

    @NotBlank
    @Size(max = 10)
    private String seatNumber;

    @NotNull
    private SeatType seatType;

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }
}