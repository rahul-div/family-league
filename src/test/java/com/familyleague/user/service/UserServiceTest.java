package com.familyleague.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.exception.ForbiddenException;
import com.familyleague.common.exception.ResourceNotFoundException;
import com.familyleague.user.dto.UpdateProfileRequest;
import com.familyleague.user.dto.UserResponse;
import com.familyleague.user.entity.User;
import com.familyleague.user.entity.UserRole;
import com.familyleague.user.repository.UserRepository;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserServiceImpl userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .username("testuser").email("test@example.com")
                .displayName("Test User").role(UserRole.USER).isActive(true).build();
        testUser.setId(userId);
        testUser.setCreatedAt(Instant.now());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        UserResponse response = userService.getUserById(userId);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllUsers_withSearch() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.searchUsers(anyString(), any())).thenReturn(page);

        var result = userService.getAllUsers("test", PageRequest.of(0, 10));
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).username()).isEqualTo("testuser");
    }

    @Test
    void updateProfile_ownerCanUpdate() {
        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(userId);
            mocked.when(SecurityUser::isAdmin).thenReturn(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenReturn(testUser);

            UserResponse result = userService.updateProfile(userId,
                    new UpdateProfileRequest("New Name", "https://avatar.png"));

            assertThat(testUser.getDisplayName()).isEqualTo("New Name");
            assertThat(testUser.getAvatarUrl()).isEqualTo("https://avatar.png");
        }
    }

    @Test
    void updateProfile_nonOwnerNonAdmin_throws() {
        UUID otherUserId = UUID.randomUUID();
        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(otherUserId);
            mocked.when(SecurityUser::isAdmin).thenReturn(false);

            assertThatThrownBy(() -> userService.updateProfile(userId,
                    new UpdateProfileRequest("Hack", null)))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    @Test
    void deleteUser_softDeletes() {
        try (MockedStatic<SecurityUser> mocked = mockStatic(SecurityUser.class)) {
            mocked.when(SecurityUser::currentUserId).thenReturn(UUID.randomUUID());
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any())).thenReturn(testUser);

            userService.deleteUser(userId);

            assertThat(testUser.isDeleted()).isTrue();
            assertThat(testUser.isActive()).isFalse();
        }
    }
}
