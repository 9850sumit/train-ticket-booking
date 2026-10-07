package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.CoachRequest;
import com.trainbooking.dto.CoachResponse;

public interface CoachService {

    CoachResponse createCoach(
            Long trainId,
            CoachRequest request,
            String adminEmail);

    List<CoachResponse> getCoachesByTrain(
            Long trainId);

    CoachResponse getCoachById(
            Long coachId);

    CoachResponse updateCoach(
            Long coachId,
            CoachRequest request,
            String adminEmail);

    void deactivateCoach(
            Long coachId,
            String adminEmail);
}