package com.trainbooking.dto;

import com.trainbooking.entity.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingPassengerResponse {

    private Long id;
    private String passengerName;
    private Integer age;
    private Gender gender;
}