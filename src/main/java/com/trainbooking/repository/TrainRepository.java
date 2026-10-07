package com.trainbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import com.trainbooking.entity.Train;
import com.trainbooking.entity.User;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    boolean existsByTrainNumber(String trainNumber);

    List<Train> findByActiveTrue();

    List<Train> findByCreatedByAndActiveTrue(User createdBy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Train t where t.id = :trainId")
    Optional<Train> findByIdForUpdate(Long trainId);
}