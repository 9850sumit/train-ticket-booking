package com.trainbooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trainbooking.entity.Train;
import com.trainbooking.entity.TrainRoute;

public interface TrainRouteRepository extends JpaRepository<TrainRoute, Long> {

    List<TrainRoute> findByTrainAndActiveTrue(Train train);

    boolean existsByTrainAndRouteName(Train train, String routeName);
}