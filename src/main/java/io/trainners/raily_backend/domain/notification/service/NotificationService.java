package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.model.dto.NotificationResponse;
import io.trainners.raily_backend.domain.notification.model.dto.UnreadCountResponse;
import io.trainners.raily_backend.domain.notification.model.entity.Notification;
import io.trainners.raily_backend.domain.notification.repository.NotificationRepository;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// 알림함
// 알림의 원본은 서버 DB, 푸시는 빠르게 알려주는 수단 -> 푸시를 놓친 사용자는 여기서 지난 알림을 확인
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림함 목록 (최근 50건만, 최신순)
    // 페이징X. 알림이 감시 1건당 최대 1개씩만 생겨 양이 많지 않고, 프론트도 무한 스크롤 없이 한 번에 렌더한다
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String email) {
        return notificationRepository.findTop50ByUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(n -> NotificationResponse.from(n))
                .toList();
    }

    //탭바 배지용. 프론트가 30초마다 폴링한다
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String email) {
        long count = notificationRepository.countByUserEmailAndReadFalse(email);
        return new UnreadCountResponse(count);
    }

    // 읽음 처리
    // findByIdAndUserEmail로 조회하므로 남의 알림 id를 넣으면 403이 아니라 404가 나간다
    // 403을 주면 "그 id는 존재한다"는 사실이 새어나간다
    @Transactional
    public void markAsRead(String email, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserEmail(notificationId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 영속 상태이므로 save() 없이 더티 체킹으로 UPDATE 된다
        // markAsRead 내부에 if (!this.read) 가 있어 두 번 호출해도 readAt 이 덮어써지지 않는다
        notification.markAsRead();
    }
}