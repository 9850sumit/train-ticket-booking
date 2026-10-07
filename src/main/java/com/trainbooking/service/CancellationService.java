package com.trainbooking.service;

import com.trainbooking.dto.CancellationRequest;
import com.trainbooking.dto.CancellationResponse;

public interface CancellationService {

    CancellationResponse cancelBooking(
            Long bookingId,
            CancellationRequest request,
            String userEmail
    );

    CancellationResponse getCancellation(
            Long bookingId,
            String userEmail
    );
}