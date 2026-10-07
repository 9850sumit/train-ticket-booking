package com.trainbooking.service.impl;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.TrainRequest;
import com.trainbooking.dto.TrainResponse;
import com.trainbooking.entity.Role;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.TrainService;

@Service
@Transactional
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    public TrainServiceImpl(
            TrainRepository trainRepository,
            UserRepository userRepository) {

        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TrainResponse createTrain(
            TrainRequest request,
            String adminEmail) {

        User admin = getAdmin(adminEmail);

        if (trainRepository.existsByTrainNumber(
                request.getTrainNumber())) {

            throw new DuplicateResourceException(
                    "Train number already exists");
        }

        Train train = new Train();

        train.setTrainNumber(
                request.getTrainNumber());

        train.setTrainName(
                request.getTrainName());

        train.setTrainType(
                request.getTrainType());

        train.setActive(true);

        train.setCreatedBy(admin);

        try {

            return mapToResponse(
                    trainRepository.save(train));

        } catch (DataIntegrityViolationException ex) {

            throw new DuplicateResourceException(
                    "Train number already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainResponse> getAllActiveTrains() {

        return trainRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainResponse> getMyTrains(
            String adminEmail) {

        User admin = getAdmin(adminEmail);

        return trainRepository
                .findByCreatedByAndActiveTrue(admin)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainResponse getTrainById(Long id) {

        Train train = trainRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + id));

        return mapToResponse(train);
    }

    @Override
    public TrainResponse updateTrain(
            Long id,
            TrainRequest request,
            String adminEmail) {

        Train train = trainRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + id));

        User admin = getAdmin(adminEmail);

        validateOwnership(train, admin);

        if (!Boolean.TRUE.equals(
                train.getActive())) {

            throw new IllegalStateException(
                    "Train is inactive");
        }

        if (!train.getTrainNumber().equals(
                request.getTrainNumber())
                && trainRepository.existsByTrainNumber(
                        request.getTrainNumber())) {

            throw new DuplicateResourceException(
                    "Train number already exists");
        }

        train.setTrainNumber(
                request.getTrainNumber());

        train.setTrainName(
                request.getTrainName());

        train.setTrainType(
                request.getTrainType());

        train.setUpdatedBy(admin);

        try {

            return mapToResponse(
                    trainRepository.save(train));

        } catch (DataIntegrityViolationException ex) {

            throw new DuplicateResourceException(
                    "Train number already exists");
        }
    }

    @Override
    public void deactivateTrain(
            Long id,
            String adminEmail) {

        Train train = trainRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + id));

        User admin = getAdmin(adminEmail);

        validateOwnership(train, admin);

        if (!Boolean.TRUE.equals(
                train.getActive())) {

            throw new IllegalStateException(
                    "Train is already inactive");
        }

        train.setActive(false);

        train.setUpdatedBy(admin);

        trainRepository.save(train);
    }

    private User getAdmin(
            String adminEmail) {

        User admin = userRepository
                .findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin user not found"));

        if (admin.getRole() != Role.ADMIN) {

            throw new AccessDeniedException(
                    "Only administrators can manage trains");
        }

        if (!Boolean.TRUE.equals(
                admin.getActive())) {

            throw new AccessDeniedException(
                    "Admin account is inactive");
        }

        return admin;
    }

    private void validateOwnership(
            Train train,
            User admin) {

        if (train.getCreatedBy() == null
                || !train.getCreatedBy()
                        .getId()
                        .equals(admin.getId())) {

            throw new AccessDeniedException(
                    "You can only manage trains created by you");
        }
    }

    private TrainResponse mapToResponse(
            Train train) {

        TrainResponse response =
                new TrainResponse();

        response.setId(
                train.getId());

        response.setTrainNumber(
                train.getTrainNumber());

        response.setTrainName(
                train.getTrainName());

        response.setTrainType(
                train.getTrainType());

        response.setActive(
                train.getActive());

        if (train.getCreatedBy() != null) {

            response.setCreatedBy(
                    train.getCreatedBy().getId());
        }

        if (train.getUpdatedBy() != null) {

            response.setUpdatedBy(
                    train.getUpdatedBy().getId());
        }

        response.setCreatedAt(
                train.getCreatedAt());

        response.setUpdatedAt(
                train.getUpdatedAt());

        return response;
    }
}