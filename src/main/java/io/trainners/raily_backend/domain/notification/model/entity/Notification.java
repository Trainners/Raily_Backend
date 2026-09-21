package io.trainners.raily_backend.domain.notification.model.entity;

import io.trainners.raily_backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_notification_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String body;

    // 알림 클릭 시 프론트가 이동할 경로
    @Column(nullable = false)
    private String linkUrl;

    // 어떤 감시건에서 나온 알림인지 (FK 대신 id만 보관 → 감시건 삭제와 무관하게 알림 유지)
    private Long seatWatchId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @Builder
    private Notification(User user, NotificationType type, String title, String body,
                         String linkUrl, Long seatWatchId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.linkUrl = linkUrl;
        this.seatWatchId = seatWatchId;
        this.read = false;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 좌석 판매 알림 생성용 정적 팩토리 — 문구를 한 곳에서 관리
    public static Notification seatSold(SeatWatch watch) {
        return Notification.builder()
                .user(watch.getUser())
                .type(NotificationType.SEAT_SOLD)
                .title("좌석 판매 알림")
                .body(String.format("%s역부터 %s호차 %s 좌석이 판매되었습니다. 이동해 주세요.",
                        watch.getSoldFromStation(), displayCarNumber(watch.getCarNumber()), watch.getSeatNumber()))
                .linkUrl("/notifications")
                .seatWatchId(watch.getId())
                .build();
    }

    // 코레일 형식 "0003" -> 사용자에게 보여줄 형태 "3" (저장&조회=원본, 문구 만들 때만 0 뺌)
    private static String displayCarNumber(String carNumber) {
        try {
            return String.valueOf(Integer.parseInt(carNumber));
        } catch (NumberFormatException e) {
            return carNumber; // 숫자가 아니면 원본 그대로
        }
    }

    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }
}