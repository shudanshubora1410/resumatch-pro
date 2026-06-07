package com.resumatchpro.service;

import com.resumatchpro.model.Notification;
import com.resumatchpro.model.User;
import com.resumatchpro.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification create(User user, String title, String message,
                                Notification.NotificationType type, String redirectUrl) {
        Notification notification = Notification.builder()
                .user(user).title(title).message(message)
                .type(type).redirectUrl(redirectUrl).isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void delete(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
