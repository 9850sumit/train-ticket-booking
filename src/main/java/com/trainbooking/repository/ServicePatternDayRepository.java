package com.trainbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.ServicePattern;
import com.trainbooking.entity.ServicePatternDay;

public interface ServicePatternDayRepository extends JpaRepository<ServicePatternDay, Long> {

    List<ServicePatternDay> findByServicePatternOrderByIdAsc(ServicePattern servicePattern);

    boolean existsByServicePatternAndDayOfWeek(
            ServicePattern servicePattern,
            String dayOfWeek);

    void deleteByServicePattern(ServicePattern servicePattern);
}