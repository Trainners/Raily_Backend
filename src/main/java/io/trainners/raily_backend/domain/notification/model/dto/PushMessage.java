package io.trainners.raily_backend.domain.notification.model.dto;

import io.trainners.raily_backend.domain.notification.model.entity.Notification;

// HTTP로 노출되지 않고 서비스끼리 주고받는 객체임
// Service Worker로 전달될 푸시 payload (JSON으로 직렬화되어 암호화 전송된다)
public record PushMessage(
        String title,
        String body,
        String url, // 알림 클릭 시 이동할 경로
        String tag, // 같은 tag의 알림은 OS 알림창에서 덮어쓰기됨 (기기 여러 대일 때 중복 표시 방지)
        Long notificationId
) {
    public static PushMessage from(Notification notification) {
        return new PushMessage(
                notification.getTitle(),
                notification.getBody(),
                notification.getLinkUrl() + "?highlight=" + notification.getId(),
                buildTag(notification),
                notification.getId()
        );
    }

    // 감시건 단위로 묶어 덮어쓰기되도록 한다.
    // seatWatchId가 없는 알림(향후 다른 유형)은 알림 id로 대체해 "seat-watch-null"을 피한다.
    private static String buildTag(Notification notification) {
        if(notification.getSeatWatchId() == null) {
            return "notification-" + notification.getId();
        }
        return "seat-watch-" + notification.getSeatWatchId();
    }
}
