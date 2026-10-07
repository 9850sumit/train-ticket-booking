package com.trainbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trainbooking.dto.ScheduleRequest;
import com.trainbooking.dto.ScheduleResponse;
import com.trainbooking.service.ScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(
            ScheduleService scheduleService) {

        this.scheduleService = scheduleService;
    }

    @PostMapping("/trains/{trainId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long trainId,
            @Valid @RequestBody ScheduleRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(
                        trainId,
                        request,
                        authentication.getName()));
    }

    @GetMapping("/schedules/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(
            Authentication authentication) {

        return ResponseEntity.ok(
                scheduleService.getMySchedules(
                        authentication.getName()));
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {

        return ResponseEntity.ok(
                scheduleService.getAllSchedules());
    }

    @GetMapping("/trains/{trainId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getTrainSchedules(
            @PathVariable Long trainId) {

        return ResponseEntity.ok(
                scheduleService.getTrainSchedules(trainId));
    }

    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(
            @PathVariable Long scheduleId) {

        return ResponseEntity.ok(
                scheduleService.getScheduleById(scheduleId));
    }

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                scheduleService.updateSchedule(
                        scheduleId,
                        request,
                        authentication.getName()));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {

        scheduleService.cancelSchedule(
                scheduleId,
                authentication.getName());

        return ResponseEntity.noContent().build();
    }
}