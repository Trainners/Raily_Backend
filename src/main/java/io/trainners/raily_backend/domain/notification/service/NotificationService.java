package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.repository.NotificationRepository;
import io.trainners.raily_backend.domain.notification.repository.SeatWatchRepository;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림함 조회·읽음 처리는 다음 단계에서 추가
}
