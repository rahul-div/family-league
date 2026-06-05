package com.familyleague.user.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.familyleague.common.dto.PagedResponse;
import com.familyleague.user.dto.UpdateProfileRequest;
import com.familyleague.user.dto.UserResponse;

public interface UserService {

    PagedResponse<UserResponse> getAllUsers(String query, Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse updateProfile(UUID id, UpdateProfileRequest request);

    void deleteUser(UUID id);
}
