package com.trainbooking.service.impl;

import com.trainbooking.dto.BookingPassengerResponse;
import com.trainbooking.dto.BookingRequest;
import com.trainbooking.dto.BookingResponse;
import com.trainbooking.dto.BookingSeatResponse;
import com.trainbooking.dto.PassengerRequest;
import com.trainbooking.dto.PaymentResponse;
import com.trainbooking.entity.Booking;
import com.trainbooking.entity.BookingPassenger;
import com.trainbooking.entity.BookingSeat;
import com.trainbooking.entity.BookingSeatStatus;
import com.trainbooking.entity.BookingStatus;
import com.trainbooking.entity.Payment;
import com.trainbooking.entity.PaymentMethod;
import com.trainbooking.entity.PaymentStatus;
import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.User;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.BookingPassengerRepository;
import com.trainbooking.repository.BookingRepository;
import com.trainbooking.repository.BookingSeatRepository;
import com.trainbooking.repository.PaymentRepository;
import com.trainbooking.repository.ScheduleRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final BigDecimal FARE_PER_SEAT =
            BigDecimal.valueOf(500);

    private final BookingRepository bookingRepository;
    private final BookingPassengerRepository bookingPassengerRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(
            BookingRequest request,
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

        Schedule schedule =
                scheduleRepository.findById(
                        request.getScheduleId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Schedule not found"));

        if (schedule.getStatus()
                != ScheduleStatus.SCHEDULED) {

            throw new IllegalStateException(
                    "Only scheduled journeys can be booked");
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        if (schedule.getJourneyDate()
                .isBefore(today)) {

            throw new IllegalStateException(
                    "Journey date has already passed");
        }

        if (request.getScheduleSeatIds() == null
                || request.getScheduleSeatIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one seat must be selected");
        }

        if (request.getPassengers() == null
                || request.getPassengers().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one passenger is required");
        }

        if (request.getScheduleSeatIds().size()
                != request.getPassengers().size()) {

            throw new IllegalArgumentException(
                    "Number of passengers must match number of selected seats");
        }

        Set<Long> uniqueSeatIds =
                new HashSet<>(
                        request.getScheduleSeatIds());

        if (uniqueSeatIds.size()
                != request.getScheduleSeatIds().size()) {

            throw new IllegalArgumentException(
                    "Duplicate seats are not allowed");
        }

        if (request.getPaymentMethod()
                != PaymentMethod.SIMULATED) {

            throw new IllegalArgumentException(
                    "Only simulated payment is currently available");
        }

        validatePassengers(
                request.getPassengers());

        List<ScheduleSeat> lockedSeats =
                new ArrayList<>();

        LocalDateTime now =
                LocalDateTime.now(BUSINESS_ZONE);

        for (Long scheduleSeatId
                : request.getScheduleSeatIds()) {

            if (scheduleSeatId == null) {
                throw new IllegalArgumentException(
                        "Schedule seat ID cannot be null");
            }

            ScheduleSeat scheduleSeat =
                    scheduleSeatRepository
                            .findByIdForUpdate(
                                    scheduleSeatId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Schedule seat not found: "
                                                    + scheduleSeatId));

            if (!scheduleSeat.getSchedule()
                    .getId()
                    .equals(schedule.getId())) {

                throw new IllegalArgumentException(
                        "Seat does not belong to the selected schedule");
            }

            if (scheduleSeat.getStatus()
                    != ScheduleSeatStatus.HELD) {

                throw new IllegalStateException(
                        "Seat "
                                + scheduleSeat.getSeat()
                                        .getSeatNumber()
                                + " is not held");
            }

            if (scheduleSeat.getHeldBy() == null
                    || !scheduleSeat.getHeldBy()
                            .getId()
                            .equals(user.getId())) {

                throw new IllegalStateException(
                        "Seat "
                                + scheduleSeat.getSeat()
                                        .getSeatNumber()
                                + " is not held by you");
            }

            if (scheduleSeat.getHeldUntil() == null
                    || !scheduleSeat.getHeldUntil()
                            .isAfter(now)) {

                scheduleSeat.setStatus(
                        ScheduleSeatStatus.AVAILABLE);

                scheduleSeat.setHeldBy(null);
                scheduleSeat.setHeldUntil(null);

                scheduleSeatRepository.save(
                        scheduleSeat);

                throw new IllegalStateException(
                        "Seat "
                                + scheduleSeat.getSeat()
                                        .getSeatNumber()
                                + " hold has expired");
            }

            lockedSeats.add(scheduleSeat);
        }

        BigDecimal totalFare =
                FARE_PER_SEAT.multiply(
                        BigDecimal.valueOf(
                                lockedSeats.size()));

        Booking booking =
                new Booking();

        booking.setPnr(generatePnr());
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setBookingDate(now);
        booking.setStatus(
                BookingStatus.PENDING);
        booking.setTotalFare(totalFare);

        booking =
                bookingRepository.save(
                        booking);

        List<BookingPassenger> passengers =
                new ArrayList<>();

        for (PassengerRequest passengerRequest
                : request.getPassengers()) {

            BookingPassenger passenger =
                    new BookingPassenger();

            passenger.setBooking(booking);

            passenger.setPassengerName(
                    passengerRequest
                            .getPassengerName()
                            .trim());

            passenger.setAge(
                    passengerRequest.getAge());

            passenger.setGender(
                    passengerRequest.getGender());

            passengers.add(
                    bookingPassengerRepository
                            .save(passenger));
        }

        BigDecimal farePerSeat =
                totalFare.divide(
                        BigDecimal.valueOf(
                                lockedSeats.size()),
                        2,
                        RoundingMode.HALF_UP);

        List<BookingSeat> bookingSeats =
                new ArrayList<>();

        for (int i = 0;
                i < lockedSeats.size();
                i++) {

            ScheduleSeat scheduleSeat =
                    lockedSeats.get(i);

            BookingSeat bookingSeat =
                    new BookingSeat();

            bookingSeat.setBooking(booking);
            bookingSeat.setPassenger(
                    passengers.get(i));
            bookingSeat.setScheduleSeat(
                    scheduleSeat);
            bookingSeat.setFare(
                    farePerSeat);
            bookingSeat.setStatus(
                    BookingSeatStatus.CONFIRMED);

            bookingSeats.add(
                    bookingSeatRepository
                            .save(bookingSeat));
        }

        Payment payment =
                new Payment();

        payment.setBooking(booking);
        payment.setTransactionId(
                generateTransactionId());
        payment.setAmount(totalFare);
        payment.setPaymentMethod(
                PaymentMethod.SIMULATED);
        payment.setStatus(
                PaymentStatus.SUCCESS);
        payment.setPaymentDate(now);

        payment =
                paymentRepository.save(
                        payment);

        for (ScheduleSeat scheduleSeat
                : lockedSeats) {

            scheduleSeat.setStatus(
                    ScheduleSeatStatus.BOOKED);

            scheduleSeat.setHeldBy(null);
            scheduleSeat.setHeldUntil(null);

            scheduleSeatRepository.save(
                    scheduleSeat);
        }

        booking.setStatus(
                BookingStatus.CONFIRMED);

        booking =
                bookingRepository.save(
                        booking);

        return mapToResponse(
                booking,
                passengers,
                bookingSeats,
                payment);
    }

    private void validatePassengers(
            List<PassengerRequest> passengers) {

        for (PassengerRequest passenger
                : passengers) {

            if (passenger == null) {
                throw new IllegalArgumentException(
                        "Passenger details are required");
            }

            if (passenger.getPassengerName() == null
                    || passenger.getPassengerName()
                            .trim()
                            .isEmpty()) {

                throw new IllegalArgumentException(
                        "Passenger name is required");
            }

            if (passenger.getPassengerName()
                    .trim()
                    .length() > 100) {

                throw new IllegalArgumentException(
                        "Passenger name is too long");
            }

            if (passenger.getAge() == null
                    || passenger.getAge() < 1
                    || passenger.getAge() > 120) {

                throw new IllegalArgumentException(
                        "Passenger age must be between 1 and 120");
            }

            if (passenger.getGender() == null) {
                throw new IllegalArgumentException(
                        "Passenger gender is required");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(
            Long bookingId,
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        Booking booking =
                bookingRepository.findById(
                        bookingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found"));

        if (!booking.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalStateException(
                    "You are not allowed to view this booking");
        }

        return buildResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByPnr(
            String pnr,
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        Booking booking =
                bookingRepository.findByPnr(pnr)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Booking not found"));

        if (!booking.getUser()
                .getId()
                .equals(user.getId())) {

            throw new IllegalStateException(
                    "You are not allowed to view this booking");
        }

        return buildResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(
            String userEmail) {

        User user =
                userRepository.findByEmail(userEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"));

        return bookingRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private BookingResponse buildResponse(
            Booking booking) {

        List<BookingPassenger> passengers =
                bookingPassengerRepository
                        .findByBooking(booking);

        List<BookingSeat> bookingSeats =
                bookingSeatRepository
                        .findByBooking(booking);

        Payment payment =
                paymentRepository
                        .findByBooking(booking)
                        .orElse(null);

        return mapToResponse(
                booking,
                passengers,
                bookingSeats,
                payment);
    }

    private BookingResponse mapToResponse(
            Booking booking,
            List<BookingPassenger> passengers,
            List<BookingSeat> bookingSeats,
            Payment payment) {

        BookingResponse response =
                new BookingResponse();

        response.setId(booking.getId());

        response.setPnr(
                booking.getPnr());

        response.setUserId(
                booking.getUser().getId());

        response.setScheduleId(
                booking.getSchedule().getId());

        response.setBookingDate(
                booking.getBookingDate());

        response.setStatus(
                booking.getStatus());

        response.setTotalFare(
                booking.getTotalFare());

        response.setCreatedAt(
                booking.getCreatedAt());

        response.setUpdatedAt(
                booking.getUpdatedAt());

        List<BookingPassengerResponse>
                passengerResponses =
                passengers.stream()
                        .map(this::mapPassenger)
                        .toList();

        response.setPassengers(
                passengerResponses);

        List<BookingSeatResponse>
                seatResponses =
                bookingSeats.stream()
                        .map(this::mapBookingSeat)
                        .toList();

        response.setSeats(
                seatResponses);

        if (payment != null) {
            response.setPayment(
                    mapPayment(payment));
        }

        return response;
    }

    private BookingPassengerResponse mapPassenger(
            BookingPassenger passenger) {

        BookingPassengerResponse response =
                new BookingPassengerResponse();

        response.setId(
                passenger.getId());

        response.setPassengerName(
                passenger.getPassengerName());

        response.setAge(
                passenger.getAge());

        response.setGender(
                passenger.getGender());

        return response;
    }

    private BookingSeatResponse mapBookingSeat(
            BookingSeat bookingSeat) {

        BookingSeatResponse response =
                new BookingSeatResponse();

        response.setId(
                bookingSeat.getId());

        response.setScheduleSeatId(
                bookingSeat
                        .getScheduleSeat()
                        .getId());

        response.setCoachNumber(
                bookingSeat
                        .getScheduleSeat()
                        .getSeat()
                        .getCoach()
                        .getCoachNumber());

        response.setSeatNumber(
                bookingSeat
                        .getScheduleSeat()
                        .getSeat()
                        .getSeatNumber());

        response.setFare(
                bookingSeat.getFare());

        response.setStatus(
                bookingSeat.getStatus());

        return response;
    }

    private PaymentResponse mapPayment(
            Payment payment) {

        PaymentResponse response =
                new PaymentResponse();

        response.setId(
                payment.getId());

        response.setTransactionId(
                payment.getTransactionId());

        response.setAmount(
                payment.getAmount());

        response.setPaymentMethod(
                payment.getPaymentMethod());

        response.setStatus(
                payment.getStatus());

        response.setPaymentDate(
                payment.getPaymentDate());

        return response;
    }

    private String generatePnr() {

        String pnr;

        do {
            pnr =
                    "PNR"
                            + UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 10)
                                    .toUpperCase();

        } while (
                bookingRepository.existsByPnr(pnr)
        );

        return pnr;
    }

    private String generateTransactionId() {

        return "TXN"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 15)
                        .toUpperCase();
    }
}