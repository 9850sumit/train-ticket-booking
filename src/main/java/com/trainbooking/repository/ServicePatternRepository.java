package com.trainbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.ServicePattern;
import com.trainbooking.entity.ServicePatternType;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;

public interface ServicePatternRepository extends JpaRepository<ServicePattern, Long> {

    List<ServicePattern> findByTrainAndActiveTrue(Train train);

    List<ServicePattern> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    List<ServicePattern> findByTrainOrderByCreatedAtDesc(Train train);

    List<ServicePattern> findByPatternTypeAndActiveTrue(ServicePatternType patternType);

    Optional<ServicePattern> findByTrainAndPatternTypeAndEffectiveFrom(
            Train train,
            ServicePatternType patternType,
            java.time.LocalDate effectiveFrom);
}