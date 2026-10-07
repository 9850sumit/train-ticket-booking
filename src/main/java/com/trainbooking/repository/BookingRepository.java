package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import com.trainbooking.entity.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    boolean existsByPnr(String pnr);

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select b
        from Booking b
        where b.id = :bookingId
    """)
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId);
}