package io.trainners.raily_backend.domain.notification.model.dto;

import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatchStatus;

// 좌석 감시 등록 성공 시 들려줄 응답
public record SeatWatchCreateResponse(
        Long id, // 생성된 감시 id
        SeatWatchStatus status // 항상 ACTIVE지만, 프론트가 상태 필드를 일관되게 다루도록 포함
) {
    public static SeatWatchCreateResponse from(SeatWatch seatWatch) {
        return new SeatWatchCreateResponse(
                seatWatch.getId(),
                seatWatch.getStatus()
        );
    }
}
