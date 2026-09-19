package io.trainners.raily_backend.domain.notification.model.dto;

import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatchStatus;

import java.time.LocalDateTime;

// 프론트의 인앱 폴링 폴백이 쓸 응답 (푸시 권한을 거부했거나 iOS 미설치 이용자는 이걸로만 알림을 받을 수 있다.)
public record SeatWatchStatusResponse(
        Long id,                        // 감시 id
        String trainNumber,             // 열차 번호
        String carNumber,               // 호차
        String seatNumber,              // 좌석 번호
        String fromStation,             // 착석역
        String toStation,               // 하차역
        SeatWatchStatus status,         // ACTIVE / NOTIFIED / EXPIRED / CANCELED
        String soldFromStation,         // 판매 감지된 역 (미감지 시 null)
        LocalDateTime notifiedAt        // 알림 시각 (미감지 시 null)
) {
    public static SeatWatchStatusResponse from(SeatWatch seatWatch) {
        return new SeatWatchStatusResponse(
                seatWatch.getId(),
                seatWatch.getTrainNumber(),
                seatWatch.getCarNumber(),
                seatWatch.getSeatNumber(),
                seatWatch.getFromStation(),
                seatWatch.getToStation(),
                seatWatch.getStatus(),
                seatWatch.getSoldFromStation(),
                seatWatch.getNotifiedAt()
        );
    }
}
