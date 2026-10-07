package com.trainbooking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SeatHoldResponse {

    private Long scheduleSeatId;
    private Long scheduleId;
    private Long seatId;
    private String coachNumber;
    private String seatNumber;
    private String seatType;
    private String status;
    private Long heldBy;
    private LocalDateTime heldUntil;
}