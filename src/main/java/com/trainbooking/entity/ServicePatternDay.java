package com.trainbooking.entity;

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
    name = "service_pattern_days",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_service_pattern_day",
            columnNames = {"service_pattern_id", "day_of_week"}
        )
    }
)
public class ServicePatternDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_pattern_id", nullable = false)
    private ServicePattern servicePattern;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek;

    public Long getId() {
        return id;
    }

    public ServicePattern getServicePattern() {
        return servicePattern;
    }

    public void setServicePattern(ServicePattern servicePattern) {
        this.servicePattern = servicePattern;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}