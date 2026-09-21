package io.trainners.raily_backend.domain.notification.controller;

import io.trainners.raily_backend.domain.notification.model.dto.NotificationResponse;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchCreateResponse;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchStatusResponse;
import io.trainners.raily_backend.domain.notification.model.dto.UnreadCountResponse;
import io.trainners.raily_backend.domain.notification.service.NotificationService;
import io.trainners.raily_backend.domain.notification.service.SeatWatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SeatWatchService seatWatchService;
    private final NotificationService notificationService;

    // ---------- 좌석 감시 ----------

    /**
     * 착석 = 감시 등록. 기존 ACTIVE 감시는 서버가 자동으로 취소한다.
     */
    @PostMapping("/seat-watch")
    public ResponseEntity<SeatWatchCreateResponse> createSeatWatch(
            Authentication authentication,
            @RequestBody @Valid SeatWatchRequest request
    ) {
        SeatWatchCreateResponse response = seatWatchService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 인앱 폴링용 상태 조회.
     * 푸시를 못 받는 사용자(권한 거부, iOS 홈 화면 미설치)에게는 이 경로가 유일한 통지 수단이다.
     */
    @GetMapping("/seat-watch/{seatWatchId}")
    public SeatWatchStatusResponse getSeatWatch(
            Authentication authentication,
            @PathVariable Long seatWatchId
    ) {
        return seatWatchService.getStatus(authentication.getName(), seatWatchId);
    }

    /**
     * 자리 비움 = 감시 취소.
     * 남의 감시건 id를 넣으면 404가 나간다(존재 여부를 숨기기 위함).
     */
    @DeleteMapping("/seat-watch/{seatWatchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSeatWatch(
            Authentication authentication,
            @PathVariable Long seatWatchId
    ) {
        seatWatchService.cancel(authentication.getName(), seatWatchId);
    }

    // ---------- 알림함 ----------

    /**
     * 알림함 목록. 최근 50건, 최신순.
     */
    @GetMapping
    public List<NotificationResponse> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    /**
     * 탭바 배지용 안 읽은 개수. 프론트가 30초마다 폴링한다.
     */
    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(Authentication authentication) {
        return notificationService.getUnreadCount(authentication.getName());
    }

    /**
     * 읽음 처리. 두 번 호출해도 readAt 이 덮어써지지 않는다.
     */
    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(authentication.getName(), notificationId);
    }
}