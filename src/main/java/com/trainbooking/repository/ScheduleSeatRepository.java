package com.trainbooking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trainbooking.entity.ScheduleSeat;
import com.trainbooking.entity.ScheduleSeatStatus;

import jakarta.persistence.LockModeType;

public interface ScheduleSeatRepository
        extends JpaRepository<ScheduleSeat, Long> {

    List<ScheduleSeat> findByScheduleOrderBySeatSeatNumber(
            com.trainbooking.entity.Schedule schedule
    );

    Optional<ScheduleSeat> findByScheduleIdAndSeatId(
            Long scheduleId,
            Long seatId
    );

    boolean existsByScheduleIdAndSeatId(
            Long scheduleId,
            Long seatId
    );

    List<ScheduleSeat> findByScheduleIdAndStatus(
            Long scheduleId,
            ScheduleSeatStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ss
            FROM ScheduleSeat ss
            WHERE ss.id = :scheduleSeatId
            """)
    Optional<ScheduleSeat> findByIdForUpdate(
            @Param("scheduleSeatId") Long scheduleSeatId
    );

    @Query("""
            SELECT COUNT(ss)
            FROM ScheduleSeat ss
            WHERE ss.schedule.id = :scheduleId
            AND (
                ss.status = com.trainbooking.entity.ScheduleSeatStatus.AVAILABLE
                OR (
                    ss.status = com.trainbooking.entity.ScheduleSeatStatus.HELD
                    AND ss.heldUntil <= :now
                )
            )
            """)
    long countAvailableOrExpiredHeldSeats(
            @Param("scheduleId") Long scheduleId,
            @Param("now") LocalDateTime now
    );
}