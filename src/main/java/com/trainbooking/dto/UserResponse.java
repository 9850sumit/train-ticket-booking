package com.trainbooking.dto;

import java.time.LocalDateTime;

import com.trainbooking.entity.Role;

public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, String phone,
                        Role role, Boolean active,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}