package com.trainbooking.repository;

import com.trainbooking.entity.Booking;
import com.trainbooking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findByBooking(Booking booking);
}