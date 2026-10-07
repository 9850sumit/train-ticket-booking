package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.ScheduleRequest;
import com.trainbooking.dto.ScheduleResponse;

public interface ScheduleService {

    ScheduleResponse createSchedule(
            Long trainId,
            ScheduleRequest request,
            String adminEmail);

    List<ScheduleResponse> getMySchedules(
            String adminEmail);

    List<ScheduleResponse> getAllSchedules();

    List<ScheduleResponse> getTrainSchedules(
            Long trainId);

    ScheduleResponse getScheduleById(
            Long scheduleId);

    ScheduleResponse updateSchedule(
            Long scheduleId,
            ScheduleRequest request,
            String adminEmail);

    void cancelSchedule(
            Long scheduleId,
            String adminEmail);
}