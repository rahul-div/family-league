package com.familyleague.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.familyleague.auth.security.SecurityUser;
import com.familyleague.common.dto.ApiResponse;
import com.familyleague.common.dto.PagedResponse;
import com.familyleague.notification.dto.BulkNotificationRequest;
import com.familyleague.notification.dto.EmailNotificationResponse;
import com.familyleague.notification.service.EmailNotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Email notification management")
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    public NotificationController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping
    @Operation(summary = "List all email logs (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<EmailNotificationResponse>>> getAll(
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(emailNotificationService.getNotificationLogs(pageable)));
    }

    @GetMapping("/me")
    @Operation(summary = "List my email notifications")
    public ResponseEntity<ApiResponse<PagedResponse<EmailNotificationResponse>>> getMine(
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                emailNotificationService.getMyNotifications(SecurityUser.currentUserId(), pageable)));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Send bulk notification (admin only)")
    public ResponseEntity<ApiResponse<Void>> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        emailNotificationService.sendBulkNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Bulk notification queued"));
    }
}
