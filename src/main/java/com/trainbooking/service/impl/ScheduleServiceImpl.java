package com.trainbooking.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.dto.ScheduleRequest;
import com.trainbooking.dto.ScheduleResponse;
import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;
import com.trainbooking.exception.DuplicateResourceException;
import com.trainbooking.exception.ResourceNotFoundException;
import com.trainbooking.repository.ScheduleRepository;
import com.trainbooking.repository.TrainRepository;
import com.trainbooking.repository.UserRepository;
import com.trainbooking.service.ScheduleService;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final ScheduleRepository scheduleRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;

    public ScheduleServiceImpl(
            ScheduleRepository scheduleRepository,
            TrainRepository trainRepository,
            UserRepository userRepository) {

        this.scheduleRepository = scheduleRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ScheduleResponse createSchedule(
            Long trainId,
            ScheduleRequest request,
            String adminEmail) {

        validateJourneyDate(request.getJourneyDate());

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));

        if (!Boolean.TRUE.equals(train.getActive())) {
            throw new ResourceNotFoundException(
                    "Train is inactive");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));

        if (train.getCreatedBy() == null ||
                !train.getCreatedBy().getId().equals(admin.getId())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only schedule trains created by you");
        }

        if (scheduleRepository.existsByTrainAndJourneyDate(
                train,
                request.getJourneyDate())) {

            throw new DuplicateResourceException(
                    "Schedule already exists for this train and date");
        }

        Schedule schedule = new Schedule();

        schedule.setTrain(train);
        schedule.setJourneyDate(request.getJourneyDate());
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedBy(admin);

        return mapToResponse(
                scheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules(
            String adminEmail) {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));

        return scheduleRepository
                .findByCreatedByOrderByJourneyDateAsc(admin)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {

        return scheduleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getTrainSchedules(
            Long trainId) {

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Train not found with id: " + trainId));

        return scheduleRepository
                .findByTrainOrderByJourneyDateAsc(train)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(
            Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found with id: "
                                        + scheduleId));

        return mapToResponse(schedule);
    }

    @Override
    public ScheduleResponse updateSchedule(
            Long scheduleId,
            ScheduleRequest request,
            String adminEmail) {

        validateJourneyDate(request.getJourneyDate());

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found with id: "
                                        + scheduleId));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));

        if (!schedule.getCreatedBy().getId()
                .equals(admin.getId())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only update your own schedules");
        }

        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Only scheduled journeys can be updated");
        }

        if (!schedule.getJourneyDate()
                .equals(request.getJourneyDate())) {

            if (scheduleRepository.existsByTrainAndJourneyDate(
                    schedule.getTrain(),
                    request.getJourneyDate())) {

                throw new DuplicateResourceException(
                        "Schedule already exists for this train and date");
            }
        }

        schedule.setJourneyDate(request.getJourneyDate());
        schedule.setUpdatedBy(admin);

        return mapToResponse(
                scheduleRepository.save(schedule));
    }

    @Override
    public void cancelSchedule(
            Long scheduleId,
            String adminEmail) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Schedule not found with id: "
                                        + scheduleId));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found"));

        if (!schedule.getCreatedBy().getId()
                .equals(admin.getId())) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only cancel your own schedules");
        }

        if (schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Schedule is already cancelled");
        }

        if (schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed schedules cannot be cancelled");
        }

        schedule.setStatus(ScheduleStatus.CANCELLED);
        schedule.setUpdatedBy(admin);

        scheduleRepository.save(schedule);
    }

    private void validateJourneyDate(
            LocalDate journeyDate) {

        if (journeyDate == null) {
            throw new IllegalArgumentException(
                    "Journey date is required");
        }

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        if (journeyDate.isBefore(today)) {
            throw new IllegalArgumentException(
                    "Journey date cannot be in the past");
        }
    }

    private ScheduleResponse mapToResponse(
            Schedule schedule) {

        ScheduleResponse response = new ScheduleResponse();

        response.setId(schedule.getId());
        response.setTrainId(schedule.getTrain().getId());
        response.setTrainNumber(
                schedule.getTrain().getTrainNumber());
        response.setTrainName(
                schedule.getTrain().getTrainName());
        response.setJourneyDate(
                schedule.getJourneyDate());
        response.setStatus(
                schedule.getStatus());
        response.setCreatedBy(
                schedule.getCreatedBy().getId());

        if (schedule.getUpdatedBy() != null) {
            response.setUpdatedBy(
                    schedule.getUpdatedBy().getId());
        }

        response.setCreatedAt(
                schedule.getCreatedAt());
        response.setUpdatedAt(
                schedule.getUpdatedAt());

        return response;
    }
}