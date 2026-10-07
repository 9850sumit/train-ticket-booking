package com.trainbooking.dto;

import com.trainbooking.entity.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequest {

    @NotNull
    private Long scheduleId;

    @NotEmpty
    private List<Long> scheduleSeatIds;

    @NotEmpty
    @Valid
    private List<PassengerRequest> passengers;

    @NotNull
    private PaymentMethod paymentMethod;
}