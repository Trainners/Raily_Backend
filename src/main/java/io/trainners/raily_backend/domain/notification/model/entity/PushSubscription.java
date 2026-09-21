package io.trainners.raily_backend.domain.notification.model.entity;

import io.trainners.raily_backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 기기 1대 = 1행, 사용자 1:N 관계
@Entity
@Table(name = "push_subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_push_subscription_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User user;

    // 브라우저마다 고유한 발송 주소, unique 제약
    @Column(nullable = false, unique = true, length = 1000)
    private String endpoint; // 이 기기로 보내려면 여기로 POST

    // 브라우저 공개키(ECDH), payload 암호화에 사용
    @Column(nullable = false)
    private String p256dh;

    // 인증 비밀값, payload 암호화에 사용
    @Column(nullable = false)
    private String auth;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private PushSubscription(User user, String endpoint, String p256dh, String auth) {
        this.user = user;
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
    }

    // 같은 기기에서 다른 계정으로 로그인해 재구독하면 소유자와 키를 갱신
    public void update(User user, String p256dh, String auth) {
        this.user = user;
        this.p256dh = p256dh;
        this.auth = auth;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}