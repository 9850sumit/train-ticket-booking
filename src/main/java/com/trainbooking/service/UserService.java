package com.trainbooking.service;

import java.util.List;

import com.trainbooking.dto.AdminCreateRequest;
import com.trainbooking.dto.AdminResponse;
import com.trainbooking.dto.AdminUpdateRequest;
import com.trainbooking.dto.RegisterRequest;
import com.trainbooking.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    List<AdminResponse> getAdmins();

    AdminResponse createAdmin(AdminCreateRequest request);

    AdminResponse updateAdmin(Long adminId, AdminUpdateRequest request);

    void deactivateAdmin(Long adminId, String requesterEmail);
}