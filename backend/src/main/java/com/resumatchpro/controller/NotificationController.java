package com.resumatchpro.controller;

import com.resumatchpro.dto.response.ApiResponse;
import com.resumatchpro.model.*;
import com.resumatchpro.service.*;
import com.resumatchpro.repository.UserRepository;
import com.resumatchpro.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse> getAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notificationService.getNotifications(extractUserId(auth), PageRequest.of(page, size))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Unread count", notificationService.getUnreadCount(extractUserId(auth))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllRead(Authentication auth) {
        notificationService.markAllAsRead(extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("All marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) throw new UnauthorizedAccessException("Not authenticated");
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }
}
