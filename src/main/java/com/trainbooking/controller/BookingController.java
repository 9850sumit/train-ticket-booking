package com.trainbooking.controller;

import com.trainbooking.dto.BookingRequest;
import com.trainbooking.dto.BookingResponse;
import com.trainbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication
    ) {

        BookingResponse response = bookingService.createBooking(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookingService.getMyBookings(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookingService.getBookingById(
                        bookingId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getBookingByPnr(
            @PathVariable String pnr,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookingService.getBookingByPnr(
                        pnr,
                        authentication.getName()
                )
        );
    }
}