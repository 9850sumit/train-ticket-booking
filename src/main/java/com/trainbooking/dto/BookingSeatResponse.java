package com.trainbooking.dto;

import com.trainbooking.entity.BookingSeatStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookingSeatResponse {

    private Long id;
    private Long scheduleSeatId;
    private String coachNumber;
    private String seatNumber;
    private BigDecimal fare;
    private BookingSeatStatus status;
}