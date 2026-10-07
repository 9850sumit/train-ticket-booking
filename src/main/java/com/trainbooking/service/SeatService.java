package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.SeatRequest;
import com.trainbooking.dto.SeatResponse;

public interface SeatService {

    SeatResponse createSeat(
            Long coachId,
            SeatRequest request,
            String adminEmail);

    List<SeatResponse> getSeatsByCoach(
            Long coachId);

    SeatResponse getSeatById(
            Long seatId);

    SeatResponse updateSeat(
            Long seatId,
            SeatRequest request,
            String adminEmail);

    void deactivateSeat(
            Long seatId,
            String adminEmail);
}