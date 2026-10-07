package com.trainbooking.controller;

import com.trainbooking.dto.CancellationRequest;
import com.trainbooking.dto.CancellationResponse;
import com.trainbooking.service.CancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<CancellationResponse> cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody(required = false) CancellationRequest request,
            Authentication authentication) {

        CancellationResponse response =
                cancellationService.cancelBooking(
                        bookingId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<CancellationResponse> getCancellation(
            @PathVariable Long bookingId,
            Authentication authentication) {

        CancellationResponse response =
                cancellationService.getCancellation(
                        bookingId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}