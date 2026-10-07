package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.SeatRequest;
import com.trainbooking.dto.SeatResponse;
import com.trainbooking.service.SeatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;

    public SeatController(
            SeatService seatService) {

        this.seatService = seatService;
    }

    @PostMapping("/coaches/{coachId}/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> createSeat(
            @PathVariable Long coachId,
            @Valid @RequestBody SeatRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(seatService.createSeat(
                        coachId,
                        request,
                        authentication.getName()));
    }

    @GetMapping("/coaches/{coachId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(
            @PathVariable Long coachId) {

        return ResponseEntity.ok(
                seatService.getSeatsByCoach(coachId));
    }

    @GetMapping("/seats/{seatId}")
    public ResponseEntity<SeatResponse> getSeat(
            @PathVariable Long seatId) {

        return ResponseEntity.ok(
                seatService.getSeatById(seatId));
    }

    @PutMapping("/seats/{seatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long seatId,
            @Valid @RequestBody SeatRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                seatService.updateSeat(
                        seatId,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/seats/{seatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateSeat(
            @PathVariable Long seatId,
            Authentication authentication) {

        seatService.deactivateSeat(
                seatId,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}