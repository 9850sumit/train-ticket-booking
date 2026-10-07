package com.trainbooking.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainbooking.entity.Schedule;
import com.trainbooking.entity.ScheduleStatus;
import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByTrainAndJourneyDate(
            Train train,
            LocalDate journeyDate);

    boolean existsByTrainAndJourneyDate(
            Train train,
            LocalDate journeyDate);

    List<Schedule> findByTrainOrderByJourneyDateAsc(
            Train train);

    List<Schedule> findByCreatedByOrderByJourneyDateAsc(
            User createdBy);

    List<Schedule> findByStatusAndJourneyDate(
            ScheduleStatus status,
            LocalDate journeyDate);

    List<Schedule> findByJourneyDateAndStatus(
            LocalDate journeyDate,
            ScheduleStatus status);

    List<Schedule> findByJourneyDateBeforeAndStatusIn(
            LocalDate journeyDate,
            List<ScheduleStatus> statuses);

    @Modifying
    @Query("""
        UPDATE Schedule s
        SET s.status = com.trainbooking.entity.ScheduleStatus.COMPLETED,
            s.updatedAt = :now
        WHERE s.journeyDate < :today
          AND s.status IN :statuses
    """)
    int completePastSchedules(
            @Param("today") LocalDate today,
            @Param("now") LocalDateTime now,
            @Param("statuses") List<ScheduleStatus> statuses);
}