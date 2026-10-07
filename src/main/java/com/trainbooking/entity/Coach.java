package com.trainbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "coaches",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_train_coach_number",
            columnNames = {"train_id", "coach_number"}
        )
    }
)
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "coach_number", nullable = false, length = 20)
    private String coachNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false)
    private CoachType coachType;

    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity;

    @Column(nullable = false)
    private Boolean active = true;

    public Coach() {
    }

    public Long getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public String getCoachNumber() {
        return coachNumber;
    }

    public void setCoachNumber(String coachNumber) {
        this.coachNumber = coachNumber;
    }

    public CoachType getCoachType() {
        return coachType;
    }

    public void setCoachType(CoachType coachType) {
        this.coachType = coachType;
    }

    public Integer getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(Integer seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}