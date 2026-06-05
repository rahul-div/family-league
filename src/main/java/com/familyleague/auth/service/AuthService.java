package com.familyleague.auth.service;

import com.familyleague.auth.dto.AuthResponse;
import com.familyleague.auth.dto.LoginRequest;
import com.familyleague.auth.dto.RegisterRequest;
import com.familyleague.user.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse createAdmin(RegisterRequest request);

    AuthResponse refreshToken();

    UserResponse getCurrentUser();
}
