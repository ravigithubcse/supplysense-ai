package com.supplysense.controller;

import com.supplysense.model.Notification;
import com.supplysense.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Org-ID") UUID orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getUserNotifications(
                userId, orgId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Org-ID") UUID orgId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId, orgId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(notificationService.markAsRead(id, userId));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader("X-Org-ID") UUID orgId) {
        notificationService.markAllAsRead(userId, orgId);
        return ResponseEntity.ok().build();
    }
}
