package com.trainbooking.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import com.trainbooking.entity.ServicePattern;
import com.trainbooking.entity.ServicePatternDay;
import com.trainbooking.entity.ServicePatternType;
import com.trainbooking.repository.ServicePatternDayRepository;

@Component
public class ServicePatternMatcher {

    private final ServicePatternDayRepository servicePatternDayRepository;

    public ServicePatternMatcher(
            ServicePatternDayRepository servicePatternDayRepository) {
        this.servicePatternDayRepository = servicePatternDayRepository;
    }

    public boolean runsOnDate(
            ServicePattern pattern,
            LocalDate date) {

        if (pattern == null || date == null) {
            return false;
        }

        if (!Boolean.TRUE.equals(pattern.getActive())) {
            return false;
        }

        if (date.isBefore(pattern.getEffectiveFrom())) {
            return false;
        }

        if (pattern.getEffectiveUntil() != null
                && date.isAfter(pattern.getEffectiveUntil())) {
            return false;
        }

        ServicePatternType type = pattern.getPatternType();

        if (type == ServicePatternType.DAILY) {
            return true;
        }

        if (type == ServicePatternType.ONE_TIME) {
            return date.equals(pattern.getEffectiveFrom());
        }

        if (type == ServicePatternType.SELECTED_DAYS) {

            DayOfWeek requestedDay = date.getDayOfWeek();

            List<ServicePatternDay> patternDays =
                    servicePatternDayRepository
                            .findByServicePatternOrderByIdAsc(pattern);

            return patternDays.stream()
                    .anyMatch(day ->
                            day.getDayOfWeek()
                                    .equalsIgnoreCase(
                                            requestedDay.name()));
        }

        return false;
    }
}