package com.trainbooking.service;

import com.trainbooking.dto.LoginRequest;
import com.trainbooking.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}