package io.trainners.raily_backend.domain.notification.model.dto;

import io.trainners.raily_backend.domain.notification.model.entity.Notification;
import io.trainners.raily_backend.domain.notification.model.entity.NotificationType;

import java.time.LocalDateTime;

// 알림함 목록 & 단건 조회 응답
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        String linkUrl, // 알림 클릭 시 이동할 경로
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLinkUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
