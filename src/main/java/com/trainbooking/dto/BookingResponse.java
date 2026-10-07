package com.trainbooking.dto;

import com.trainbooking.entity.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingResponse {

    private Long id;
    private String pnr;

    private Long userId;
    private Long scheduleId;

    private LocalDateTime bookingDate;
    private BookingStatus status;
    private BigDecimal totalFare;

    private List<BookingPassengerResponse> passengers;
    private List<BookingSeatResponse> seats;
    private PaymentResponse payment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}