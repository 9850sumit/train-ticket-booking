package com.trainbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TrainRouteRequest {

    @NotBlank(message = "Route name is required")
    @Size(max = 150, message = "Route name cannot exceed 150 characters")
    private String routeName;

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }
}