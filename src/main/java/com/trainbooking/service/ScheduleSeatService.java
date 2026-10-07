package com.trainbooking.service;

import com.trainbooking.dto.ScheduleSeatResponse;
import com.trainbooking.dto.SeatHoldResponse;

import java.util.List;

public interface ScheduleSeatService {

    List<ScheduleSeatResponse> initializeScheduleSeats(
            Long scheduleId,
            String adminEmail
    );

    List<ScheduleSeatResponse> getScheduleSeats(
            Long scheduleId
    );

    SeatHoldResponse holdSeat(
            Long scheduleSeatId,
            String userEmail
    );
}