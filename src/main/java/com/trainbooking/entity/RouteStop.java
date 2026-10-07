package com.trainbooking.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "route_stops",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_route_stop_sequence",
            columnNames = {"train_route_id", "stop_sequence"}
        )
    }
)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_route_id", nullable = false)
    private TrainRoute trainRoute;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "distance_from_source", precision = 10, scale = 2)
    private BigDecimal distanceFromSource;

    public RouteStop() {
    }

    public Long getId() {
        return id;
    }

    public TrainRoute getTrainRoute() {
        return trainRoute;
    }

    public void setTrainRoute(TrainRoute trainRoute) {
        this.trainRoute = trainRoute;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(Integer stopSequence) {
        this.stopSequence = stopSequence;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public BigDecimal getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(BigDecimal distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }
}