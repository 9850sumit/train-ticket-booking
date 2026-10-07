package com.trainbooking.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleLifecycleService {

    private final ScheduleLifecycleUpdater lifecycleUpdater;

    public ScheduleLifecycleService(
            ScheduleLifecycleUpdater lifecycleUpdater) {
        this.lifecycleUpdater = lifecycleUpdater;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void updateLifecycleOnStartup() {
        lifecycleUpdater.completePastSchedules();
    }

    @Scheduled(
            cron = "0 5 0 * * *",
            zone = "Asia/Kolkata")
    public void scheduledLifecycleUpdate() {
        lifecycleUpdater.completePastSchedules();
    }
}