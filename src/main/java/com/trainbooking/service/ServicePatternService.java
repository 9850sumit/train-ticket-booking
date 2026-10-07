package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.ServicePatternRequest;
import com.trainbooking.dto.ServicePatternResponse;

public interface ServicePatternService {

    ServicePatternResponse createPattern(
            Long trainId,
            ServicePatternRequest request,
            String adminEmail);

    List<ServicePatternResponse> getMyPatterns(
            String adminEmail);

    List<ServicePatternResponse> getTrainPatterns(
            Long trainId);

    ServicePatternResponse getPatternById(
            Long patternId);

    ServicePatternResponse updatePattern(
            Long patternId,
            ServicePatternRequest request,
            String adminEmail);

    void deactivatePattern(
            Long patternId,
            String adminEmail);
}