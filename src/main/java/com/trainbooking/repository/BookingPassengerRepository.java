package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import com.trainbooking.entity.BookingPassenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPassengerRepository extends JpaRepository<BookingPassenger, Long> {

    List<BookingPassenger> findByBooking(Booking booking);
}