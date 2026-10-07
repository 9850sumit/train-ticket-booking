package com.trainbooking.dto;

import com.trainbooking.entity.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerRequest {

    @NotBlank
    @Size(max = 100)
    private String passengerName;

    @NotNull
    @Min(1)
    @Max(120)
    private Integer age;

    @NotNull
    private Gender gender;
}