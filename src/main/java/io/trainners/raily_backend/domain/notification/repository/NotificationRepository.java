package io.trainners.raily_backend.domain.notification.repository;

import io.trainners.raily_backend.domain.notification.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 알림함
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 최근 50건만 (알림이 무한정 켜지지 않게), 내 알림만, 최신순 정렬로
    List<Notification> findTop50ByUserEmailOrderByCreatedAtDesc(String email);
    // 내 알림 중 read == false인 것의 개수(안 읽은 알림 배지용)
    long countByUserEmailAndReadFalse(String email);
    // 읽음 처리할 때 소유자 검사 겸 조회
    Optional<Notification> findByIdAndUserEmail(Long id, String email);
}