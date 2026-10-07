package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.BookingRequest;
import com.trainbooking.dto.BookingResponse;

public interface BookingService {

    BookingResponse createBooking(
            BookingRequest request,
            String userEmail
    );

    BookingResponse getBookingById(
            Long bookingId,
            String userEmail
    );

    BookingResponse getBookingByPnr(
            String pnr,
            String userEmail
    );

    List<BookingResponse> getMyBookings(
            String userEmail
    );
}