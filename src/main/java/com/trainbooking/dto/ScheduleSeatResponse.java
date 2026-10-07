package com.trainbooking.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleSeatResponse {

    private Long id;
    private Long scheduleId;
    private Long seatId;
    private String coachNumber;
    private String seatNumber;
    private String seatType;
    private String status;
    private LocalDateTime heldUntil;
    private Long heldBy;
    private Integer version;
}