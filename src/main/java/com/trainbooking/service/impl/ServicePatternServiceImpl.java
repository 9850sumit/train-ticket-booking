package com.trainbooking.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.ServicePatternRequest;
import com.trainbooking.dto.ServicePatternResponse;
import com.trainbooking.entity.Role;
import com.trainbooking.entity.ServicePattern;
import com.trainbooking.entity.ServicePatternDay;
import com.trainbooking.entity.ServicePatternType;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.ServicePatternDayRepository;
import com.trainbooking.repository.ServicePatternRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.ServicePatternService;

@Service
@Transactional
public class ServicePatternServiceImpl implements ServicePatternService {

    private final ServicePatternRepository servicePatternRepository;
    private final ServicePatternDayRepository servicePatternDayRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    public ServicePatternServiceImpl(
            ServicePatternRepository servicePatternRepository,
            ServicePatternDayRepository servicePatternDayRepository,
            TrainRepository trainRepository,
            UserRepository userRepository) {

        this.servicePatternRepository = servicePatternRepository;
        this.servicePatternDayRepository = servicePatternDayRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ServicePatternResponse createPattern(
            Long trainId,
            ServicePatternRequest request,
            String adminEmail) {

        Train train = getTrain(trainId);
        User admin = getAdmin(adminEmail);

        validateTrainOwnership(train, admin);
        validateRequest(request);

        if (servicePatternRepository
                .findByTrainAndPatternTypeAndEffectiveFrom(
                        train,
                        request.getPatternType(),
                        request.getEffectiveFrom())
                .isPresent()) {

            throw new DuplicateResourceException(
                    "A service pattern with the same type and effective date already exists for this train");
        }

        ServicePattern pattern = new ServicePattern();

        pattern.setTrain(train);
        pattern.setPatternType(request.getPatternType());
        pattern.setEffectiveFrom(request.getEffectiveFrom());
        pattern.setEffectiveUntil(request.getEffectiveUntil());
        pattern.setActive(true);
        pattern.setCreatedBy(admin);

        ServicePattern savedPattern =
                servicePatternRepository.save(pattern);

        savePatternDays(savedPattern, request);

        return mapToResponse(savedPattern);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePatternResponse> getMyPatterns(
            String adminEmail) {

        User admin = getAdmin(adminEmail);

        return servicePatternRepository
                .findByCreatedByOrderByCreatedAtDesc(admin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicePatternResponse> getTrainPatterns(
            Long trainId) {

        Train train = getTrain(trainId);

        return servicePatternRepository
                .findByTrainOrderByCreatedAtDesc(train)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePatternResponse getPatternById(
            Long patternId) {

        ServicePattern pattern = getPattern(patternId);

        return mapToResponse(pattern);
    }

    @Override
    public ServicePatternResponse updatePattern(
            Long patternId,
            ServicePatternRequest request,
            String adminEmail) {

        ServicePattern pattern = getPattern(patternId);
        User admin = getAdmin(adminEmail);

        validatePatternOwnership(pattern, admin);
        validateRequest(request);

        if (!pattern.getActive()) {
            throw new IllegalStateException(
                    "Inactive service patterns cannot be updated");
        }

        boolean changedDefinition =
                pattern.getPatternType() != request.getPatternType()
                || !pattern.getEffectiveFrom()
                        .equals(request.getEffectiveFrom());

        if (changedDefinition) {

            servicePatternRepository
                    .findByTrainAndPatternTypeAndEffectiveFrom(
                            pattern.getTrain(),
                            request.getPatternType(),
                            request.getEffectiveFrom())
                    .ifPresent(existing -> {

                        if (!existing.getId()
                                .equals(pattern.getId())) {

                            throw new DuplicateResourceException(
                                    "A service pattern with the same type and effective date already exists for this train");
                        }
                    });
        }

        pattern.setPatternType(request.getPatternType());
        pattern.setEffectiveFrom(request.getEffectiveFrom());
        pattern.setEffectiveUntil(request.getEffectiveUntil());
        pattern.setUpdatedBy(admin);

        servicePatternDayRepository
                .deleteByServicePattern(pattern);

        savePatternDays(pattern, request);

        ServicePattern savedPattern =
                servicePatternRepository.save(pattern);

        return mapToResponse(savedPattern);
    }

    @Override
    public void deactivatePattern(
            Long patternId,
            String adminEmail) {

        ServicePattern pattern = getPattern(patternId);
        User admin = getAdmin(adminEmail);

        validatePatternOwnership(pattern, admin);

        if (!pattern.getActive()) {
            throw new IllegalStateException(
                    "Service pattern is already inactive");
        }

        pattern.setActive(false);
        pattern.setUpdatedBy(admin);

        servicePatternRepository.save(pattern);
    }

    private Train getTrain(Long trainId) {

        return trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));
    }

    private User getAdmin(String email) {

        User user = userRepository.findByEmail(
                        email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin user not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new IllegalStateException(
                    "Only administrators can manage service patterns");
        }

        if (!user.getActive()) {
            throw new IllegalStateException(
                    "Inactive administrator cannot manage service patterns");
        }

        return user;
    }

    private ServicePattern getPattern(Long patternId) {

        return servicePatternRepository.findById(patternId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Service pattern not found with id: "
                                        + patternId));
    }

    private void validateTrainOwnership(
            Train train,
            User admin) {

        if (!train.getActive()) {
            throw new IllegalStateException(
                    "Cannot create a service pattern for an inactive train");
        }

        if (train.getCreatedBy() == null
                || !train.getCreatedBy().getId()
                        .equals(admin.getId())) {

            throw new IllegalStateException(
                    "You can manage service patterns only for trains created by you");
        }
    }

    private void validatePatternOwnership(
            ServicePattern pattern,
            User admin) {

        if (pattern.getCreatedBy() == null
                || !pattern.getCreatedBy().getId()
                        .equals(admin.getId())) {

            throw new IllegalStateException(
                    "You can manage only service patterns created by you");
        }
    }

    private void validateRequest(
            ServicePatternRequest request) {

        if (request.getPatternType() == null) {
            throw new IllegalArgumentException(
                    "Pattern type is required");
        }

        if (request.getEffectiveFrom() == null) {
            throw new IllegalArgumentException(
                    "Effective from date is required");
        }

        if (request.getEffectiveUntil() != null
                && request.getEffectiveUntil()
                        .isBefore(request.getEffectiveFrom())) {

            throw new IllegalArgumentException(
                    "Effective until date cannot be before effective from date");
        }

        List<String> days =
                request.getDaysOfWeek() == null
                        ? Collections.emptyList()
                        : request.getDaysOfWeek();

        switch (request.getPatternType()) {

            case DAILY:

                if (!days.isEmpty()) {
                    throw new IllegalArgumentException(
                            "DAILY pattern must not contain selected days");
                }

                break;

            case SELECTED_DAYS:

                if (days.isEmpty()) {
                    throw new IllegalArgumentException(
                            "SELECTED_DAYS pattern requires at least one day");
                }

                validateDays(days);

                break;

            case ONE_TIME:

                if (request.getEffectiveUntil() != null
                        && !request.getEffectiveUntil()
                                .equals(request.getEffectiveFrom())) {

                    throw new IllegalArgumentException(
                            "ONE_TIME pattern cannot have a date range");
                }

                if (!days.isEmpty()) {
                    throw new IllegalArgumentException(
                            "ONE_TIME pattern must not contain selected days");
                }

                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported service pattern type");
        }
    }

    private void validateDays(List<String> days) {

        Set<String> normalizedDays = new HashSet<>();

        for (String day : days) {

            if (day == null || day.isBlank()) {
                throw new IllegalArgumentException(
                        "Day of week cannot be empty");
            }

            String normalized =
                    day.trim().toUpperCase(Locale.ROOT);

            try {
                DayOfWeek.valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        "Invalid day of week: " + day);
            }

            if (!normalizedDays.add(normalized)) {
                throw new IllegalArgumentException(
                        "Duplicate day of week: " + normalized);
            }
        }
    }

    private void savePatternDays(
            ServicePattern pattern,
            ServicePatternRequest request) {

        if (request.getPatternType()
                != ServicePatternType.SELECTED_DAYS) {

            return;
        }

        for (String day : request.getDaysOfWeek()) {

            ServicePatternDay patternDay =
                    new ServicePatternDay();

            patternDay.setServicePattern(pattern);
            patternDay.setDayOfWeek(
                    day.trim().toUpperCase(Locale.ROOT));

            servicePatternDayRepository.save(patternDay);
        }
    }

    private ServicePatternResponse mapToResponse(
            ServicePattern pattern) {

        ServicePatternResponse response =
                new ServicePatternResponse();

        response.setId(pattern.getId());
        response.setTrainId(pattern.getTrain().getId());
        response.setTrainNumber(
                pattern.getTrain().getTrainNumber());
        response.setTrainName(
                pattern.getTrain().getTrainName());
        response.setPatternType(pattern.getPatternType());
        response.setEffectiveFrom(
                pattern.getEffectiveFrom());
        response.setEffectiveUntil(
                pattern.getEffectiveUntil());
        response.setActive(pattern.getActive());

        List<String> days =
                servicePatternDayRepository
                        .findByServicePatternOrderByIdAsc(pattern)
                        .stream()
                        .map(ServicePatternDay::getDayOfWeek)
                        .collect(Collectors.toList());

        response.setDaysOfWeek(days);

        response.setCreatedBy(
                pattern.getCreatedBy().getId());

        if (pattern.getUpdatedBy() != null) {
            response.setUpdatedBy(
                    pattern.getUpdatedBy().getId());
        }

        response.setCreatedAt(pattern.getCreatedAt());
        response.setUpdatedAt(pattern.getUpdatedAt());

        return response;
    }
}