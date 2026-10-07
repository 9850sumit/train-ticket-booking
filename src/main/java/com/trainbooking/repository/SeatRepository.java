package com.trainbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByCoachIdAndActiveTrue(Long coachId);

    Optional<Seat> findByIdAndActiveTrue(Long id);

    boolean existsByCoachIdAndSeatNumber(
            Long coachId,
            String seatNumber);

    boolean existsByCoachIdAndSeatNumberAndIdNot(
            Long coachId,
            String seatNumber,
            Long id);
}