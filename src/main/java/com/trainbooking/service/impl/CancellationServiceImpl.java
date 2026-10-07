package com.trainbooking.service.impl;

import com.trainbooking.dto.CancellationRequest;
import com.trainbooking.dto.CancellationResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.BookingSeat;
import com.trainbooking.entity.BookingSeatStatus;
import com.trainbooking.entity.BookingStatus;
import com.trainbooking.entity.Cancellation;
import com.trainbooking.entity.CancellationStatus;
import com.trainbooking.entity.Payment;
import com.trainbooking.entity.PaymentStatus;
import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.User;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.BookingRepository;
import com.trainbooking.repository.BookingSeatRepository;
import com.trainbooking.repository.CancellationRepository;
import com.trainbooking.repository.PaymentRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.CancellationService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancellationServiceImpl
        implements CancellationService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final CancellationRepository cancellationRepository;
    private final PaymentRepository paymentRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CancellationResponse cancelBooking(
            Long bookingId,
            CancellationRequest request,
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException(
                    "User account is inactive");
        }

        /*
         * Lock the booking before checking its status.
         *
         * This prevents two simultaneous cancellation
         * requests from cancelling the same booking.
         */
        Booking booking =
                bookingRepository.findByIdForUpdate(bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found"));

        if (!booking.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalStateException(
                    "You are not allowed to cancel this booking");
        }

        if (booking.getStatus()
                == BookingStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Booking is already cancelled");
        }

        if (booking.getStatus()
                != BookingStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Only confirmed bookings can be cancelled");
        }

        /*
         * Cancellation is allowed only while the journey
         * is still scheduled.
         */
        if (booking.getSchedule().getStatus()
                != ScheduleStatus.SCHEDULED) {

            throw new IllegalStateException(
                    "Booking cannot be cancelled after the journey has started or completed");
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        if (booking.getSchedule()
                .getJourneyDate()
                .isBefore(today)) {

            throw new IllegalStateException(
                    "Journey date has already passed");
        }

        if (cancellationRepository
                .existsByBooking(booking)) {

            throw new IllegalStateException(
                    "Cancellation already exists for this booking");
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBooking(
                        booking);

        if (bookingSeats.isEmpty()) {

            throw new IllegalStateException(
                    "No seats found for this booking");
        }

        /*
         * Lock every schedule seat before releasing it.
         */
        for (BookingSeat bookingSeat
                : bookingSeats) {

            if (bookingSeat.getStatus()
                    != BookingSeatStatus.CONFIRMED) {

                throw new IllegalStateException(
                        "Booking seat is not currently confirmed");
            }

            ScheduleSeat scheduleSeat =
                    scheduleSeatRepository
                            .findByIdForUpdate(
                                    bookingSeat
                                            .getScheduleSeat()
                                            .getId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Schedule seat not found"));

            if (scheduleSeat.getStatus()
                    != ScheduleSeatStatus.BOOKED) {

                throw new IllegalStateException(
                        "Seat "
                                + scheduleSeat.getSeat()
                                        .getSeatNumber()
                                + " is not currently booked");
            }

            scheduleSeat.setStatus(
                    ScheduleSeatStatus.AVAILABLE);

            scheduleSeat.setHeldBy(null);
            scheduleSeat.setHeldUntil(null);

            scheduleSeatRepository.save(
                    scheduleSeat);

            bookingSeat.setStatus(
                    BookingSeatStatus.CANCELLED);

            bookingSeatRepository.save(
                    bookingSeat);
        }

        Payment payment =
                paymentRepository
                        .findByBooking(booking)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found for booking"));

        if (payment.getStatus()
                != PaymentStatus.SUCCESS) {

            throw new IllegalStateException(
                    "Payment is not eligible for refund");
        }

        BigDecimal originalAmount =
                booking.getTotalFare();

        /*
         * Current application policy:
         * full refund for a confirmed cancellation.
         */
        BigDecimal refundAmount =
                originalAmount;

        payment.setStatus(
                PaymentStatus.REFUNDED);

        paymentRepository.save(payment);

        booking.setStatus(
                BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        LocalDateTime now =
                LocalDateTime.now(BUSINESS_ZONE);

        Cancellation cancellation =
                new Cancellation();

        cancellation.setBooking(booking);

        cancellation.setCancelledBy(user);

        cancellation.setReason(
                request != null
                        ? request.getReason()
                        : null);

        cancellation.setCancelledAt(now);

        cancellation.setOriginalAmount(
                originalAmount);

        cancellation.setRefundAmount(
                refundAmount);

        cancellation.setStatus(
                CancellationStatus.PROCESSED);

        cancellation =
                cancellationRepository.save(
                        cancellation);

        return mapToResponse(
                cancellation);
    }

    @Override
    @Transactional(readOnly = true)
    public CancellationResponse getCancellation(
            Long bookingId,
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        Booking booking =
                bookingRepository.findById(bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found"));

        if (!booking.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalStateException(
                    "You are not allowed to view this cancellation");
        }

        Cancellation cancellation =
                cancellationRepository
                        .findByBooking(booking)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cancellation not found"));

        return mapToResponse(
                cancellation);
    }

    private CancellationResponse mapToResponse(
            Cancellation cancellation) {

        CancellationResponse response =
                new CancellationResponse();

        response.setId(
                cancellation.getId());

        response.setBookingId(
                cancellation.getBooking()
                        .getId());

        response.setPnr(
                cancellation.getBooking()
                        .getPnr());

        response.setReason(
                cancellation.getReason());

        response.setCancelledAt(
                cancellation.getCancelledAt());

        response.setOriginalAmount(
                cancellation.getOriginalAmount());

        response.setRefundAmount(
                cancellation.getRefundAmount());

        response.setStatus(
                cancellation.getStatus());

        response.setBookingStatus(
                cancellation.getBooking()
                        .getStatus()
                        .name());

        return response;
    }
}