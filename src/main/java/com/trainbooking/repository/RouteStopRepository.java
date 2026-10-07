package com.trainbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.RouteStop;
import com.trainbooking.entity.TrainRoute;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByTrainRouteOrderByStopSequence(
            TrainRoute trainRoute);

    boolean existsByTrainRouteAndStopSequence(
            TrainRoute trainRoute,
            Integer stopSequence);

    boolean existsByTrainRouteAndStopSequenceAndIdNot(
            TrainRoute trainRoute,
            Integer stopSequence,
            Long id);
}