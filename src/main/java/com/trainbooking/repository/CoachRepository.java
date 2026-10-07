package com.trainbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.Coach;
import com.trainbooking.entity.Train;

public interface CoachRepository extends JpaRepository<Coach, Long> {

    List<Coach> findByTrainIdAndActiveTrue(Long trainId);

    Optional<Coach> findByIdAndActiveTrue(Long id);

    boolean existsByTrainIdAndCoachNumber(
            Long trainId,
            String coachNumber);

    boolean existsByTrainIdAndCoachNumberAndIdNot(
            Long trainId,
            String coachNumber,
            Long id);
}