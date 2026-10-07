package com.trainbooking.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancellationRequest {

    @Size(max = 500)
    private String reason;
}