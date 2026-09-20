package io.trainners.raily_backend.domain.notification.event;

import io.trainners.raily_backend.domain.notification.model.dto.PushMessage;
import io.trainners.raily_backend.domain.notification.repository.NotificationRepository;
import io.trainners.raily_backend.domain.notification.service.WebPushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushListener {

    private final NotificationRepository notificationRepository;
    private final WebPushSender webPushSender;

    // AFTER_COMMIT: DB 저장이 확정된 뒤에만 푸시를 보낸다. 푸시는 한 번 나가면 되돌릴 수 없는데, 트랜잭션이 롤백되면 "알림은 받았는데 알림함에는 없는" 상태가 되기 때문이다.
    // REQUIRES_NEW: 원래 트랜잭션은 이미 커밋돼 끝났으므로, 조회를 위해 새 트랜잭션이 필요하다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        notificationRepository.findById(event.notificationId())
                .ifPresentOrElse(
                        notification -> webPushSender.send(
                                notification.getUser().getId(),
                                PushMessage.from(notification)
                        ),
                        () -> log.warn("알림을 찾을 수 없음 notificationId={}", event.notificationId())
                );
    }
}