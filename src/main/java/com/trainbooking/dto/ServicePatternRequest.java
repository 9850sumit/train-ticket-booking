package com.trainbooking.dto;

import java.time.LocalDate;
import java.util.List;

import com.trainbooking.entity.ServicePatternType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public class ServicePatternRequest {

    @NotNull(message = "Pattern type is required")
    private ServicePatternType patternType;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveUntil;

    private List<@NotEmpty(message = "Day of week cannot be empty") String> daysOfWeek;

    public ServicePatternType getPatternType() {
        return patternType;
    }

    public void setPatternType(ServicePatternType patternType) {
        this.patternType = patternType;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveUntil() {
        return effectiveUntil;
    }

    public void setEffectiveUntil(LocalDate effectiveUntil) {
        this.effectiveUntil = effectiveUntil;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
}