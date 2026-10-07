package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.SeatRequest;
import com.trainbooking.dto.SeatResponse;
import com.trainbooking.entity.Coach;
import com.trainbooking.entity.Seat;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.CoachRepository;
import com.trainbooking.repository.SeatRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.SeatService;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final CoachRepository coachRepository;
    private final UserRepository userRepository;

    public SeatServiceImpl(
            SeatRepository seatRepository,
            CoachRepository coachRepository,
            UserRepository userRepository) {

        this.seatRepository = seatRepository;
        this.coachRepository = coachRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SeatResponse createSeat(
            Long coachId,
            SeatRequest request,
            String adminEmail) {

        Coach coach = coachRepository
                .findByIdAndActiveTrue(coachId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Coach not found with id: "
                                        + coachId));

        User admin = getAdmin(adminEmail);

        validateOwnership(coach, admin);

        if (seatRepository.existsByCoachIdAndSeatNumber(
                coachId,
                request.getSeatNumber())) {

            throw new DuplicateResourceException(
                    "Seat number already exists in this coach");
        }

        long existingSeats = seatRepository
                .findByCoachIdAndActiveTrue(coachId)
                .size();

        if (existingSeats >= coach.getSeatCapacity()) {

            throw new IllegalStateException(
                    "Coach seat capacity has been reached");
        }

        Seat seat = new Seat();

        seat.setCoach(coach);
        seat.setSeatNumber(
                request.getSeatNumber());
        seat.setSeatType(
                request.getSeatType());
        seat.setActive(true);

        return mapToResponse(
                seatRepository.save(seat));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByCoach(
            Long coachId) {

        if (!coachRepository.existsById(coachId)) {

            throw new ResourceNotFoundException(
                    "Coach not found with id: "
                            + coachId);
        }

        return seatRepository
                .findByCoachIdAndActiveTrue(coachId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse getSeatById(
            Long seatId) {

        Seat seat = seatRepository
                .findByIdAndActiveTrue(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found with id: "
                                        + seatId));

        return mapToResponse(seat);
    }

    @Override
    public SeatResponse updateSeat(
            Long seatId,
            SeatRequest request,
            String adminEmail) {

        Seat seat = seatRepository
                .findByIdAndActiveTrue(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found with id: "
                                        + seatId));

        Coach coach = seat.getCoach();

        User admin = getAdmin(adminEmail);

        validateOwnership(coach, admin);

        if (seatRepository
                .existsByCoachIdAndSeatNumberAndIdNot(
                        coach.getId(),
                        request.getSeatNumber(),
                        seatId)) {

            throw new DuplicateResourceException(
                    "Seat number already exists in this coach");
        }

        seat.setSeatNumber(
                request.getSeatNumber());

        seat.setSeatType(
                request.getSeatType());

        return mapToResponse(
                seatRepository.save(seat));
    }

    @Override
    public void deactivateSeat(
            Long seatId,
            String adminEmail) {

        Seat seat = seatRepository
                .findByIdAndActiveTrue(seatId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Seat not found with id: "
                                        + seatId));

        Coach coach = seat.getCoach();

        User admin = getAdmin(adminEmail);

        validateOwnership(coach, admin);

        seat.setActive(false);

        seatRepository.save(seat);
    }

    private User getAdmin(String adminEmail) {

        return userRepository
                .findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));
    }

    private void validateOwnership(
            Coach coach,
            User admin) {

        Train train = coach.getTrain();

        if (train == null ||
                train.getCreatedBy() == null ||
                !train.getCreatedBy()
                        .getId()
                        .equals(admin.getId())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only manage seats for your own train");
        }
    }

    private SeatResponse mapToResponse(
            Seat seat) {

        SeatResponse response =
                new SeatResponse();

        response.setId(seat.getId());

        response.setCoachId(
                seat.getCoach().getId());

        response.setSeatNumber(
                seat.getSeatNumber());

        response.setSeatType(
                seat.getSeatType());

        response.setActive(
                seat.getActive());

        return response;
    }
}