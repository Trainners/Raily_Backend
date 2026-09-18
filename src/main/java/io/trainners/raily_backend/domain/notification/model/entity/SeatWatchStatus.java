package io.trainners.raily_backend.domain.notification.model.entity;

public enum SeatWatchStatus {
    ACTIVE,    // 감시 중 (스케줄러 대상)
    NOTIFIED,  // 판매 감지 → 알림 발송 완료 (더 이상 감시 X, 중복 알림 방지)
    EXPIRED,   // 여정 종료 시각이 지나 자동 종료
    CANCELED   // 사용자가 "자리 비움" 또는 다른 좌석으로 이동
}
