package com.supplysense.service;

import com.supplysense.model.*;
import com.supplysense.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public Notification sendNotification(UUID userId, UUID orgId, String title, String message,
                                          NotificationChannel channel, NotificationSeverity severity,
                                          String relatedEntityId, String relatedEntityType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .orgId(orgId)
                .title(title)
                .message(message)
                .channel(channel)
                .severity(severity)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();

        notification = notificationRepository.save(notification);

        try {
            switch (channel) {
                case EMAIL   -> sendEmail(notification);
                case SLACK   -> sendSlack(notification);
                case IN_APP  -> markSent(notification);
                case WEBHOOK -> sendWebhook(notification);
                default      -> log.warn("Unsupported channel: {}", channel);
            }
        } catch (Exception e) {
            log.error("Failed to deliver notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }

        return notification;
    }

    @Transactional
    public Notification markAsRead(UUID notificationId, UUID userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
                .map(n -> {
                    n.setStatus(NotificationStatus.READ);
                    n.setReadAt(Instant.now());
                    return notificationRepository.save(n);
                })
                .orElseThrow(() -> new java.util.NoSuchElementException("Notification not found"));
    }

    @Transactional
    public void markAllAsRead(UUID userId, UUID orgId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndOrgIdAndStatusIn(userId, orgId,
                        List.of(NotificationStatus.SENT, NotificationStatus.DELIVERED));
        Instant now = Instant.now();
        unread.forEach(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(UUID userId, UUID orgId, Pageable pageable) {
        return notificationRepository.findByUserIdAndOrgIdOrderByCreatedAtDesc(userId, orgId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId, UUID orgId) {
        return notificationRepository.countByUserIdAndOrgIdAndStatusIn(userId, orgId,
                List.of(NotificationStatus.SENT, NotificationStatus.DELIVERED));
    }

    // ─── Private delivery helpers ────────────────────────────

    private void sendEmail(Notification n) {
        // In production: use a template engine (Thymeleaf) and HTML email
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("user@example.com"); // In prod: look up user email
        mail.setSubject("[SupplySense] " + n.getTitle());
        mail.setText(n.getMessage());
        mail.setFrom("noreply@supplysense.ai");
        mailSender.send(mail);
        markSent(n);
        log.info("Email notification sent: {}", n.getId());
    }

    private void sendSlack(Notification n) {
        // In production: POST to Slack Incoming Webhook URL from config
        // WebClient.create(slackWebhookUrl).post().bodyValue(...).retrieve().bodyToMono(Void.class).block();
        log.info("Slack notification queued (stub): {}", n.getTitle());
        markSent(n);
    }

    private void sendWebhook(Notification n) {
        // In production: POST to org-configured webhook URL
        log.info("Webhook notification queued (stub): {}", n.getId());
        markSent(n);
    }

    private void markSent(Notification n) {
        n.setStatus(NotificationStatus.SENT);
        n.setSentAt(Instant.now());
        notificationRepository.save(n);
    }
}
