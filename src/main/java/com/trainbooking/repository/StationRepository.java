package com.trainbooking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.Station;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByStationCode(String stationCode);

    boolean existsByStationCode(String stationCode);
}