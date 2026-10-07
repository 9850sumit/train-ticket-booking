package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.CoachRequest;
import com.trainbooking.dto.CoachResponse;
import com.trainbooking.entity.Coach;
import com.trainbooking.entity.CoachType;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.CoachRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.CoachService;
import com.trainbooking.repository.SeatRepository;

@Service
@Transactional
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    public CoachServiceImpl(
            CoachRepository coachRepository,
            TrainRepository trainRepository,
            UserRepository userRepository,
            SeatRepository seatRepository) {

        this.coachRepository = coachRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public CoachResponse createCoach(
            Long trainId,
            CoachRequest request,
            String adminEmail) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (coachRepository.existsByTrainIdAndCoachNumber(
                trainId,
                request.getCoachNumber())) {

            throw new DuplicateResourceException(
                    "Coach number already exists for this train");
        }

        CoachType coachType =
                parseCoachType(request.getCoachType());

        Coach coach = new Coach();

        coach.setTrain(train);
        coach.setCoachNumber(
                request.getCoachNumber());
        coach.setCoachType(coachType);
        coach.setSeatCapacity(
                request.getSeatCapacity());
        coach.setActive(true);

        return mapToResponse(
                coachRepository.save(coach));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachResponse> getCoachesByTrain(
            Long trainId) {

        if (!trainRepository.existsById(trainId)) {
            throw new ResourceNotFoundException(
                    "Train not found with id: " + trainId);
        }

        return coachRepository
                .findByTrainIdAndActiveTrue(trainId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachResponse getCoachById(
            Long coachId) {

        Coach coach = coachRepository
                .findByIdAndActiveTrue(coachId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Coach not found with id: "
                                        + coachId));

        return mapToResponse(coach);
    }

    @Override
    public CoachResponse updateCoach(
            Long coachId,
            CoachRequest request,
            String adminEmail) {

        Coach coach = coachRepository
                .findByIdAndActiveTrue(coachId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Coach not found with id: "
                                        + coachId));

        Train train = coach.getTrain();

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (coachRepository
                .existsByTrainIdAndCoachNumberAndIdNot(
                        train.getId(),
                        request.getCoachNumber(),
                        coachId)) {

            throw new DuplicateResourceException(
                    "Coach number already exists for this train");
        }

        CoachType coachType =
                parseCoachType(request.getCoachType());

        long existingSeats = seatRepository
                .findByCoachIdAndActiveTrue(coachId)
                .size();

        if (request.getSeatCapacity() < existingSeats) {
            throw new IllegalStateException(
                    "Seat capacity cannot be less than the number of active seats: "
                            + existingSeats);
        }

        coach.setCoachNumber(
                request.getCoachNumber());

        coach.setCoachType(coachType);

        coach.setSeatCapacity(
                request.getSeatCapacity());

        return mapToResponse(
                coachRepository.save(coach));
    }

    @Override
    public void deactivateCoach(
            Long coachId,
            String adminEmail) {

        Coach coach = coachRepository
                .findByIdAndActiveTrue(coachId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Coach not found with id: "
                                        + coachId));

        Train train = coach.getTrain();

        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);

        coach.setActive(false);

        coachRepository.save(coach);
    }

    private User getAdmin(String adminEmail) {

        return userRepository
                .findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));
    }

    private void validateTrainOwnership(
            Train train,
            User admin) {

        if (train.getCreatedBy() == null ||
                !train.getCreatedBy()
                        .getId()
                        .equals(admin.getId())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only manage coaches for trains created by you");
        }
    }

    private CoachType parseCoachType(String coachType) {
        if (coachType == null || coachType.isBlank()) {
            throw new IllegalArgumentException(
                    "Coach type is required. Allowed values: "
                    + "FIRST_AC, SECOND_AC, THIRD_AC, SL, CC, EC, FC, SECOND_SITTING");
        }

        try {
            return CoachType.valueOf(
                    coachType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid coach type. Allowed values: "
                    + "FIRST_AC, SECOND_AC, THIRD_AC, SL, CC, EC, FC, SECOND_SITTING");
        }
    }
    private CoachResponse mapToResponse(
            Coach coach) {

        CoachResponse response =
                new CoachResponse();

        response.setId(coach.getId());

        response.setTrainId(
                coach.getTrain().getId());

        response.setCoachNumber(
                coach.getCoachNumber());

        response.setCoachType(
                coach.getCoachType().name());

        response.setSeatCapacity(
                coach.getSeatCapacity());

        response.setActive(
                coach.getActive());

        return response;
    }
}