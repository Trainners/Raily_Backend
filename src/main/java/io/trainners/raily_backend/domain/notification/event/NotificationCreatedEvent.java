package io.trainners.raily_backend.domain.notification.event;

// 알림이 DB에 저장되었음을 알리는 이벤트
// 발송에 필요한 값을 통째로 담지 않고 id만 담는 이유:
// 리스너가 커밋 이후에 다시 조회하므로, 이벤트가 오래된 데이터를 들고 다닐 일이 없다.
public record NotificationCreatedEvent(Long notificationId) {
}
