package com.trainbooking.controller;

import com.trainbooking.dto.ScheduleSeatResponse;
import com.trainbooking.dto.SeatHoldResponse;
import com.trainbooking.service.ScheduleSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    @PostMapping("/{scheduleId}/seat-inventory/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScheduleSeatResponse>> initializeScheduleSeats(
            @PathVariable Long scheduleId,
            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        scheduleSeatService.initializeScheduleSeats(
                                scheduleId,
                                authentication.getName()
                        )
                );
    }

    @GetMapping("/{scheduleId}/seat-inventory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleSeatResponse>> getScheduleSeats(
            @PathVariable Long scheduleId
    ) {

        return ResponseEntity.ok(
                scheduleSeatService.getScheduleSeats(scheduleId)
        );
    }

    @PostMapping("/{scheduleId}/seat-inventory/{scheduleSeatId}/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SeatHoldResponse> holdSeat(
            @PathVariable Long scheduleId,
            @PathVariable Long scheduleSeatId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                scheduleSeatService.holdSeat(
                        scheduleSeatId,
                        authentication.getName()
                )
        );
    }
}