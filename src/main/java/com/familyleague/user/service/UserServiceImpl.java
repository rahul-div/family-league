package com.familyleague.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.common.exception.ForbiddenException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.user.dto.UpdateProfileRequest;
import com.familyleague.user.dto.UserResponse;
import com.familyleague.user.entity.User;
import com.familyleague.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(String query, Pageable pageable) {
        Page<User> page;
        if (StringUtils.hasText(query)) {
            page = userRepository.searchUsers(query, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        List<UserResponse> content = page.getContent().stream()
                .map(UserResponse::from)
                .toList();
        return PagedResponse.from(page, content);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        UUID currentUserId = SecurityUser.currentUserId();
        if (!currentUserId.equals(id) && !SecurityUser.isAdmin()) {
            throw new ForbiddenException("You can only update your own profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));

        if (StringUtils.hasText(request.displayName())) {
            user.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
        user.softDelete(SecurityUser.currentUserId());
        user.setActive(false);
        userRepository.save(user);
    }
}
