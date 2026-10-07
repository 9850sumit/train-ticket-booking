package com.trainbooking.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.ServicePattern;
import com.trainbooking.entity.Train;
import com.trainbooking.repository.CoachRepository;
import com.trainbooking.repository.ScheduleRepository;
import com.trainbooking.repository.ScheduleSeatRepository;
import com.trainbooking.repository.SeatRepository;
import com.trainbooking.repository.ServicePatternRepository;
import com.trainbooking.repository.TrainRepository;

@Service
public class ScheduleResolver {

    private final ScheduleRepository scheduleRepository;
    private final ServicePatternRepository servicePatternRepository;
    private final ServicePatternMatcher servicePatternMatcher;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final TrainRepository trainRepository;

    public ScheduleResolver(
            ScheduleRepository scheduleRepository,
            ServicePatternRepository servicePatternRepository,
            ServicePatternMatcher servicePatternMatcher,
            ScheduleSeatRepository scheduleSeatRepository,
            CoachRepository coachRepository,
            SeatRepository seatRepository,
            TrainRepository trainRepository) {

        this.scheduleRepository = scheduleRepository;
        this.servicePatternRepository = servicePatternRepository;
        this.servicePatternMatcher = servicePatternMatcher;
        this.scheduleSeatRepository = scheduleSeatRepository;
        this.coachRepository = coachRepository;
        this.seatRepository = seatRepository;
        this.trainRepository = trainRepository;
    }

    @Transactional
    public Schedule resolveSchedule(
            Train train,
            LocalDate journeyDate) {

        if (train == null || journeyDate == null) {
            return null;
        }

        if (!Boolean.TRUE.equals(train.getActive())) {
            return null;
        }

        Schedule existingSchedule =
                scheduleRepository.findByTrainAndJourneyDate(
                        train,
                        journeyDate)
                        .orElse(null);

        if (existingSchedule != null) {

            if (existingSchedule.getStatus()
                    != ScheduleStatus.SCHEDULED) {

                return null;
            }

            initializeScheduleSeats(existingSchedule);

            return existingSchedule;
        }

        ServicePattern matchingPattern =
                servicePatternRepository
                        .findByTrainAndActiveTrue(train)
                        .stream()
                        .filter(pattern ->
                                servicePatternMatcher.runsOnDate(
                                        pattern,
                                        journeyDate))
                        .findFirst()
                        .orElse(null);

        if (matchingPattern == null) {
            return null;
        }

        Train lockedTrain =
                trainRepository.findByIdForUpdate(train.getId())
                        .orElse(null);

        if (lockedTrain == null ||
                !Boolean.TRUE.equals(lockedTrain.getActive())) {
            return null;
        }

        Schedule concurrentSchedule =
                scheduleRepository.findByTrainAndJourneyDate(
                        lockedTrain,
                        journeyDate)
                        .orElse(null);

        if (concurrentSchedule != null) {

            if (concurrentSchedule.getStatus()
                    != ScheduleStatus.SCHEDULED) {

                return null;
            }

            initializeScheduleSeats(concurrentSchedule);

            return concurrentSchedule;
        }

        ServicePattern lockedMatchingPattern =
                servicePatternRepository
                        .findByTrainAndActiveTrue(lockedTrain)
                        .stream()
                        .filter(pattern ->
                                servicePatternMatcher.runsOnDate(
                                        pattern,
                                        journeyDate))
                        .findFirst()
                        .orElse(null);

        if (lockedMatchingPattern == null) {
            return null;
        }

        Schedule schedule = new Schedule();

        schedule.setTrain(lockedTrain);
        schedule.setJourneyDate(journeyDate);
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setCreatedBy(lockedTrain.getCreatedBy());

        Schedule savedSchedule =
                scheduleRepository.save(schedule);

        initializeScheduleSeats(savedSchedule);

        return savedSchedule;
    }

    private void initializeScheduleSeats(
            Schedule schedule) {

        if (schedule == null ||
                schedule.getTrain() == null) {
            return;
        }

        var coaches =
                coachRepository.findByTrainIdAndActiveTrue(
                        schedule.getTrain().getId());

        for (var coach : coaches) {

            var seats =
                    seatRepository.findByCoachIdAndActiveTrue(
                            coach.getId());

            for (var seat : seats) {

                boolean exists =
                        scheduleSeatRepository
                                .existsByScheduleIdAndSeatId(
                                        schedule.getId(),
                                        seat.getId());

                if (exists) {
                    continue;
                }

                ScheduleSeat scheduleSeat =
                        new ScheduleSeat();

                scheduleSeat.setSchedule(schedule);
                scheduleSeat.setSeat(seat);
                scheduleSeat.setStatus(
                        ScheduleSeatStatus.AVAILABLE);
                scheduleSeat.setHeldUntil(null);
                scheduleSeat.setHeldBy(null);
                scheduleSeat.setVersion(0);

                scheduleSeatRepository.save(scheduleSeat);
            }
        }
    }
}