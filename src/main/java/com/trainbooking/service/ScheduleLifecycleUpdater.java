package com.trainbooking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.repository.ScheduleRepository;

@Component
public class ScheduleLifecycleUpdater {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final ScheduleRepository scheduleRepository;

    public ScheduleLifecycleUpdater(
            ScheduleRepository scheduleRepository) {

        this.scheduleRepository = scheduleRepository;
    }

    @Scheduled(
            fixedDelay = 3600000,
            initialDelay = 5000)
    @Transactional
    public void completePastSchedules() {

        LocalDate today =
                LocalDate.now(BUSINESS_ZONE);

        LocalDateTime now =
                LocalDateTime.now(BUSINESS_ZONE);

        int updated =
                scheduleRepository.completePastSchedules(
                        today,
                        now,
                        List.of(
                                ScheduleStatus.SCHEDULED,
                                ScheduleStatus.BOARDING,
                                ScheduleStatus.IN_PROGRESS
                        )
                );

        if (updated > 0) {
            System.out.println(
                    "Schedule lifecycle updated "
                            + updated
                            + " past schedule(s) to COMPLETED."
            );
        }
    }
}